package at.bitmedia.kiga.ui.views.pages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import at.bitmedia.kiga.backend.data.dto.AnwesenheitChecklisteDTO;
import at.bitmedia.kiga.backend.data.entity.*;
import at.bitmedia.kiga.backend.repositories.*;
import at.bitmedia.kiga.ui.Constants;
import at.bitmedia.kiga.util.CommonUtils;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import at.bitmedia.kiga.SimpleI18NProvider;
import at.bitmedia.kiga.app.security.SecurityUtils;
import at.bitmedia.kiga.layout.DefaultLayout;
import at.bitmedia.kiga.ui.component.DatePickerLoc;
import at.bitmedia.kiga.ui.component.grid.GridStatusBar;
import at.bitmedia.kiga.ui.views.search.MerkmalzuordnungSearch;
import at.bitmedia.kiga.util.SearchUtils;
import at.bitmedia.kiga.util.VaadinUtils;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Anwesenheit Checkliste")
@Tag("anwesenheitcheckliste-view")
@Route(value = "anwesenheitcheckliste", layout = DefaultLayout.class)
@JsModule("./src/views/anwesenheitcheckliste.js")
public class AnwesenheitChecklisteView extends PolymerTemplate<AnwesenheitChecklisteView.Model> implements AfterNavigationObserver {

    private static final long serialVersionUID = 1L;

    @Id("search")
    private Button search;

    @Id("stichtag")
    private DatePickerLoc stichtag;

    @Id("famname")
    private TextField famname;

    @Id("vorname")
    private TextField vorname;

    @Id("groupCombo")
    private ComboBox<KB_Gruppe> groupCombo;

    @Id("grid")
    private Grid<AnwesenheitChecklisteDTO> grid;

    @Id("speichernBtn")
    private Button saveButton;

    @Id("pupilGridStatusBar")
    private GridStatusBar pupilGridStatusBar;

    private Binder<MerkmalzuordnungSearch> searchBinder = new Binder<>(MerkmalzuordnungSearch.class);
    private SimpleI18NProvider i18Nprovider;
    private SchuelerRepository schuelerRepository;
    private KbAnwesendRepository kbAnwesendRepository;

    public interface Model extends TemplateModel {
        void setLastNameLabelText(String lastNameLabelText);
        void setFirstNameLabelText(String firstNameLabelText);
        void setGroupCombo(String groupCombo);
        void setSaveButtonText(String saveButtonText);
        void setCancelButtonText(String cancelButtonText);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        getModel().setLastNameLabelText(i18Nprovider.getTranslation("Merkmalezuord.personal.Grid.secondname"));
        getModel().setFirstNameLabelText(i18Nprovider.getTranslation("Merkmalezuord.personal.Grid.firstname"));
        getModel().setGroupCombo(i18Nprovider.getTranslation("Gruppenzuordnen.tableR.group"));
        getModel().setSaveButtonText(i18Nprovider.getTranslation("Button.save"));
        getModel().setCancelButtonText(i18Nprovider.getTranslation("Button.cancel"));
    }

    @Autowired
    public AnwesenheitChecklisteView(SimpleI18NProvider i18Nprovider,
                                     KbAnwesendRepository kbAnwesendRepository,  SchuelerRepository schuelerRepository, KB_GruppeRepository kbGruppeRepository) {
        this.i18Nprovider = i18Nprovider;
        this.schuelerRepository = schuelerRepository;
        this.kbAnwesendRepository = kbAnwesendRepository;

        groupCombo.setItemLabelGenerator(KB_Gruppe::getKbgName);
        groupCombo.setClearButtonVisible(true);
        List<KB_Gruppe> groupList = kbGruppeRepository.findKB_Gruppe(SecurityUtils.getSuKennzahl(), LocalDate.now(), "%", "%");
        groupList.sort(Comparator.comparing(KB_Gruppe::getKbgNameSort));

        groupCombo.setItems(groupList);
        groupCombo.getDataProvider().refreshAll();

        searchBinder.forField(stichtag).bind(MerkmalzuordnungSearch::getStichtag, MerkmalzuordnungSearch::setStichtag);
        searchBinder.forField(famname).withNullRepresentation("").bind(MerkmalzuordnungSearch::getVorname, MerkmalzuordnungSearch::setFamname);
        searchBinder.forField(vorname).withNullRepresentation("").bind(MerkmalzuordnungSearch::getVorname, MerkmalzuordnungSearch::setVorname);

        // Configure Grids
        grid.addColumn(item -> item.getSchueler().getSgFamName()).setHeader(i18Nprovider.getTranslation("Merkmalezuord.personal.Grid.secondname"));
        grid.addColumn(item -> item.getSchueler().getSgVorname1()).setHeader(i18Nprovider.getTranslation("Merkmalezuord.personal.Grid.firstname"));
        grid.addColumn((AnwesenheitChecklisteDTO personal) -> {
            if (personal.getSchueler().getSgGeburtDat() == null)
                return "";
            else
                return personal.getSchueler().getSgGeburtDat().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }).setHeader(i18Nprovider.getTranslation("Merkmalezuord.personal.Grid.birthday"))
                .setComparator((item1, item2) -> item1.getSchueler().getSgGeburtDat().compareTo(item2.getSchueler().getSgGeburtDat()));

        grid.addComponentColumn(personal -> {
            Checkbox checkBox1 = new Checkbox();
            TimePicker fromTp = new TimePicker();
            fromTp.setClearButtonVisible(true);
            fromTp.setLocale(Locale.GERMANY);
            fromTp.addValueChangeListener(e -> {
                if (fromTp.isEmpty()) {
                    personal.setKbanVon(null);
                } else {
                    personal.setKbanVon(e.getValue().atDate(stichtag.getValue()));
                }
            });
            checkBox1.setValue(personal.getKbanVon() != null);
            fromTp.setEnabled(personal.getKbanVon() != null);
            fromTp.setErrorMessage(i18Nprovider.getTranslation("message.wrongData"));
            HorizontalLayout horizontalLayout = new HorizontalLayout(checkBox1, fromTp);
            horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, checkBox1);

            if(personal.getKbanVon() != null) {
                personal.setCheckBoxVon(true);
                updateCounterGrid();
                fromTp.setValue(CommonUtils.orElse(personal.getKbanVon().toLocalTime(), LocalTime.parse("00:00")));
            }

            checkBox1.addValueChangeListener(event -> {
                personal.setCheckBoxVon(event.getValue());
                fromTp.setEnabled(event.getValue());
               if(checkBox1.getValue() && fromTp.isEmpty()){
                   fromTp.setValue(LocalTime.now());
               }

                updateCounterGrid();
            });

            return horizontalLayout;
        }).setWidth("15%").setFlexGrow(0).setHeader(createHeaderFromCheckbox()).setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(personal -> {
            Checkbox checkBox2 = new Checkbox();
            TimePicker toTp = new TimePicker();
            toTp.setClearButtonVisible(true);
            toTp.setLocale(Locale.GERMANY);
            toTp.addValueChangeListener(e -> {
                if (toTp.isEmpty()) {
                    personal.setKbanBis(null);
                } else {
                    personal.setKbanBis(e.getValue().atDate(stichtag.getValue()));
                }
            });
            checkBox2.setValue(personal.getKbanBis() != null);
            toTp.setEnabled(personal.getKbanBis() != null);
            toTp.setErrorMessage(i18Nprovider.getTranslation("message.wrongData"));
            HorizontalLayout horizontalLayout = new HorizontalLayout(checkBox2, toTp);
            horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, checkBox2);

            if(personal.getKbanBis() != null) {
                personal.setCheckBoxBis(true);
                updateCounterGrid();
                toTp.setValue(CommonUtils.orElse(personal.getKbanBis().toLocalTime(), LocalTime.parse("00:00")));
            }

            checkBox2.addValueChangeListener(event -> {
                personal.setCheckBoxBis(event.getValue());
                toTp.setEnabled(event.getValue());
                if(checkBox2.getValue() && toTp.isEmpty()){
                    toTp.setValue(LocalTime.now());
                }

                updateCounterGrid();
            });

            return horizontalLayout;
        }).setWidth("15%").setFlexGrow(0).setHeader(createHeaderToCheckbox()).setTextAlign(ColumnTextAlign.CENTER);

        grid.addItemClickListener(e -> {
            if(!e.getItem().getSchueler().getKbAnwesends().isEmpty()) {
                pupilGridStatusBar.setStatusBarUpdatedText(
                        e.getItem().getSchueler().getKbAnwesends().get(0).getUpdatedate(),
                        e.getItem().getSchueler().getKbAnwesends().get(0).getUpdateuser());
            }
        });

        VaadinUtils.setGridSortableColumns(grid);
        VaadinUtils.setGridResizableColumns(grid);
        stichtag.setValue(LocalDate.now());
        stichtag.setPlaceholder(i18Nprovider.getTranslation("Merkamalezuord.stichtag.placeholder"));
        stichtag.addValueChangeListener(event -> {
            if (!stichtag.isEmpty()) {
                LocalDate datum2 = stichtag.isEmpty() ? LocalDate.now() : stichtag.getValue();
                List<KB_Gruppe> groupList2 = kbGruppeRepository.findKB_Gruppe(SecurityUtils.getSuKennzahl(), datum2, "%", "%");
                if (groupList2.isEmpty()) {
                    Notification.show(i18Nprovider.getTranslation("Gruppenzuordnen.emptyList"));
                }
                groupCombo.setItems(groupList2);
                groupCombo.getDataProvider().refreshAll();
            }
        });

        search.addClickListener(e -> executeSearch());
        saveButton.addClickListener(e -> executeSave());

    }

    private void updateCounterGrid(){
        pupilGridStatusBar.setStatusBarSelectedText(
                getSelectedItemsVon() + " / " + getSelectedItemsBis(),
                grid.getDataProvider().size(new Query<>()));
    }

    private long getSelectedItemsVon(){
        return grid.getDataProvider().fetch(new Query<>())
                .filter(e -> (e.isCheckBoxVon()))
                .count();
    }

    private long getSelectedItemsBis(){
        return grid.getDataProvider().fetch(new Query<>())
                .filter(e -> (e.isCheckBoxBis()))
                .count();
    }

    private Checkbox createHeaderFromCheckbox(){
        Checkbox headerCheckbox = new Checkbox(i18Nprovider.getTranslation("AnwesenheitCheckliste.fromtime"));

        headerCheckbox.addValueChangeListener(event -> {
            grid.getDataProvider().fetch(new Query<>()).forEach(e -> {
                if(event.getValue()){
                    if(e.getKbanVon() == null){
                        e.setKbanVon(LocalDateTime.now());
                    }
                } else {
                    e.setKbanVon(null);
                }
                e.setCheckBoxVon(event.getValue());
            });
            grid.getDataProvider().refreshAll();
            updateCounterGrid();
        });
        return headerCheckbox;
    }

    private Checkbox createHeaderToCheckbox(){
        Checkbox headerCheckbox = new Checkbox(i18Nprovider.getTranslation("AnwesenheitCheckliste.totime"));

        headerCheckbox.addValueChangeListener(event -> {
            grid.getDataProvider().fetch(new Query<>()).forEach(e -> {

                if(event.getValue()){
                    if(e.getKbanBis() == null){
                        e.setKbanBis(LocalDateTime.now());
                    }
                } else {
                    e.setKbanBis(null);
                }
                e.setCheckBoxBis(event.getValue());
            });
            grid.getDataProvider().refreshAll();
            updateCounterGrid();
        });
        return headerCheckbox;
    }

    private void executeSave() {
        List<AnwesenheitChecklisteDTO> selectedAnwesends = grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
        selectedAnwesends = selectedAnwesends.stream().filter(element -> element.isCheckBoxVon() || element.isCheckBoxBis()).collect(Collectors.toList());

        try {
            List<KbAnwesend> listToSaveUpdate = selectedAnwesends.stream().map(anwesendDTO -> {
                KbAnwesend recordToSave = null;
                if (!anwesendDTO.getSchueler().getKbAnwesends().isEmpty()) {
                    recordToSave = anwesendDTO.getSchueler().getKbAnwesends().get(0);
                } else {
                    recordToSave = KbAnwesend.builder().build();
                }

                recordToSave.setSgKennzahl(anwesendDTO.getSchueler().getSgKennzahl());
                recordToSave.setSuKennzahl(SecurityUtils.getSuKennzahl());
                if(anwesendDTO.isCheckBoxVon()){
                    recordToSave.setVon(anwesendDTO.getKbanVon());
                }
                if(anwesendDTO.isCheckBoxBis()){
                    recordToSave.setBis(anwesendDTO.getKbanBis());
                }

                return recordToSave;
            }).collect(Collectors.toList());

            for(KbAnwesend record : listToSaveUpdate){
                if(record.getVon() == null && record.getBis() == null){
                    kbAnwesendRepository.delete(record);
                } else {
                    kbAnwesendRepository.save(record);
                }
            }

            Notification.show(i18Nprovider.getTranslation("message.SuccessSave"));
            executeSearch();

        }
        catch (ObjectOptimisticLockingFailureException | StaleObjectStateException transactionError) {
            Notification.show(i18Nprovider.getTranslation("error.transaction"));
        } catch (Exception e) {
            Notification.show(i18Nprovider.getTranslation("KigaStat.timeIntersect"));
        }
    }

    private Object executeSearch() {
        if(stichtag.isEmpty()){
            Notification.show(i18Nprovider.getTranslation("Merkamalezuord.search.noDate"));
            return null;
        } else {

            String lastNameStr = SearchUtils.stringValueForSearch(searchBinder.forField(famname).getField().getValue().toString());
            String firstNameStr = SearchUtils.stringValueForSearch(searchBinder.forField(vorname).getField().getValue().toString());
            LocalDate date = (LocalDate) searchBinder.forField(stichtag).getField().getValue();

            List<Schueler> personalList;

            if(groupCombo.getValue() == null){
                personalList = schuelerRepository.findSchuelerByLaufbahnAndVornameAndNachnameForCheckliste(date, firstNameStr, lastNameStr, SecurityUtils.getSuKennzahl());
            } else {
                personalList = schuelerRepository.findSchuelerByLaufbahnAndVornameAndNachnameAndGroupForCheckliste(date, firstNameStr, lastNameStr, SecurityUtils.getSuKennzahl(), groupCombo.getValue().getKbgId());
            }

            List<AnwesenheitChecklisteDTO> listForGrid = getListForGrid(personalList);

            grid.setItems(listForGrid);
            grid.getDataProvider().refreshAll();
            updateCounterGrid();
            getUI().ifPresent(ui -> ui.getSession().setAttribute(Constants.KINDER_MERKMAL_SESSION, personalList));
            SearchUtils.searchResults(personalList);

            return listForGrid;
        }
    }

    private List<AnwesenheitChecklisteDTO> getListForGrid(List<Schueler> list){

        List<AnwesenheitChecklisteDTO> listForGrid = list.stream().map(schueler -> {
            AnwesenheitChecklisteDTO newRecord = AnwesenheitChecklisteDTO.builder()
                .schueler(schueler)
                .build();

            if (!schueler.getKbAnwesends().isEmpty()) {
                if((schueler.getKbAnwesends().get(0).getVon() != null && schueler.getKbAnwesends().get(0).getVon().toLocalDate().equals(stichtag.getValue()))
                        || (schueler.getKbAnwesends().get(0).getBis() != null && schueler.getKbAnwesends().get(0).getBis().toLocalDate().equals(stichtag.getValue()))){
                    newRecord.setKbanVon(schueler.getKbAnwesends().get(0).getVon());
                    newRecord.setKbanBis(schueler.getKbAnwesends().get(0).getBis());
                }
            }

            return newRecord;
        }).collect(Collectors.toList());

        return listForGrid;
    }

}
