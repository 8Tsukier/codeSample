package at.bitmedia.kiga.backend.repositories;

import java.time.LocalDate;
import java.util.List;

import at.bitmedia.kiga.backend.data.dto.AnwesenheitChecklisteDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import at.bitmedia.kiga.backend.data.entity.Schueler;

public interface SchuelerRepository extends JpaSpecificationExecutor<Schueler>, JpaRepository<Schueler, Long> {

//  public List<Schueler> findAllBySuSjSgSkAndBis(
//      @Param("suKennzahl") Integer suKennzahl,
//      @Param("sjSchuljahr") Integer sjSchuljahr,
//      @Param("sgKennzahl") Long sgKennzahl,
//      @Param("skId") Long skId,
//      @Param("bis") LocalDate bis);

//  public List<Schueler> findIlbZuordnungByLrIdAndSuKennzahlAndSjSchuljahr(
//      @Param("lrId") Long lrId,
//      @Param("suKennzahl") Integer suKennzahl,
//      @Param("sjSchuljahr") Integer sjSchuljahr);

	public List<Schueler> findSchuelerByFamnameAndVornameAll(
	    @Param("famname") String famname,
	    @Param("vorname") String vorname,
	    @Param("minAge") int minAge,
	    @Param("maxAge") int maxAge,
	    @Param("endDate") LocalDate endDate,
	    @Param("schoolIds") List<Integer> schoolIds);

	public Integer findCountByFamnameAndVornameAll(
	    @Param("famname") String famname,
	    @Param("vorname") String vorname,
		@Param("minAge") int minAge,
		@Param("maxAge") int maxAge,
		@Param("endDate") LocalDate endDate,
		@Param("schoolIds") List<Integer> schoolIds);


    public Integer findCountByFamnameAndVornameAndAge(
      @Param("famname") String famname,
      @Param("vorname") String vorname,
      @Param("years") int years,
      @Param("endDate") LocalDate endDate,
      @Param("schoolIds") List<Integer> schoolIds);

    public List<Schueler> findSchuelerByFamnameWithAllSchuelerlaufbahnSuKennzahl(
      @Param("famname") String famname, @Param("suKennzahl") Integer suKennzahl);

 	 public List<Schueler> findSchuelerByFamnameWithAllSchuelerlaufbahnSuKennzahlAndGruppe(
      @Param("famname") String famname, @Param("suKennzahl") Integer suKennzahl, 	@Param("kbgId") Long kbgId);

    public List<Schueler> findSchuelerByFamnameWithSkkkzNotEAndSuKennzahl(
      @Param("famname") String famname, @Param("suKennzahl") Integer suKennzahl);

	public List<Schueler> findSchuelerByFamnameWithSkkkzNotEAndSuKennzahlAndBeforeBis(
			@Param("famname") String famname, @Param("suKennzahl") Integer suKennzahl, @Param("bisDate") LocalDate bisDate);

    public List<Schueler> findSchuelerByKB_Gruppe(
      @Param("suKennzahl") Integer suKennzahl,
			@Param("kbgId") Long kbgId);
    
    public List<Schueler> findSchuelerByKB_GruppeAndSg(
        @Param("suKennzahl") Integer suKennzahl,
        @Param("sgKennzahl") Long sgKennzahl,
        @Param("kbgId") Long kbgId);

    public List<Schueler> findNotAssignSchuelerByKB_Gruppe(
      @Param("suKennzahl") Integer suKennzahl);
    
    public List<Schueler> findAssignSchuelerByKB_Gruppe(
      @Param("suKennzahl") Integer suKennzahl);  

	public List<Schueler> findSchuelerBySuKennzahl(@Param("suKennzahl") Integer suKennzahl);

	/**
	 * Find all children assigned to group and modules with stichtag between von/bis dates of these modules 
	 * allows to set kbgId parameter equal null (equivalent to kbgId like '%')
	 * @param suKennzahl - Integer
	 * @param kbgId - Long or can be null
	 * @param stichtag - LocalDate or can be null too
	 * @return
	 */
	public List<Schueler> findSchuelerBySuAndGroupWithModulesInDate(
	    @Param("suKennzahl") Integer suKennzahl,
	    @Param("kbgId") Long kbgId,
	    @Param("stichtag") LocalDate stichtag,
	    @Param("dayOfWeek") String dayOfWeek);
	
	public List<Schueler> findDistinctSchuelerBySgKennzahl(@Param("sgKennzahl") Long sgKennzahl);
	
	// @Query("SELECT coalesce(max(sch.id), :n_SG_KENNZAHL) +1 FROM Schueler sch
	// WHERE sch.id BETWEEN :n_SG_KENNZAHL AND (:n_SG_KENNZAHL + 9999)")
	public Long findMaxId(@Param("n_SG_KENNZAHL") String Id);

	@Query("SELECT coalesce(max(sch.id), 0) +1 FROM Schueler sch WHERE sch.id > :n_SG_KENNZAHL AND sch.id < (:n_SG_KENNZAHL + 9999)")
	public Long findMaxId2(@Param("n_SG_KENNZAHL") Long Id);

	@Query("SELECT coalesce(max(sch.id), 0 ) FROM Schueler sch WHERE sch.id = :n_SG_KENNZAHL")
	public Long getTestId(@Param("n_SG_KENNZAHL") Long Id);

	public Schueler findFirstBySgBpknr(String SG_BPKNR);

	// @Query("SELECT DISTINCT sch FROM Schueler sch WHERE a.aktzordList az WHERE
	// az.id.grName=:group")
	public Schueler findFirstBySgFamNameAndSgVorname1(@Param("sgFamName") String sgFamName,
			@Param("sgVorname1") String sgVorname1);

	public Schueler findFirstBySgBpknrOrSgFamNameAndSgVorname1(@Param("sgBpknr") String sgBpknr,
			@Param("sgFamName") String sgFamName, @Param("sgVorname1") String sgVorname1);

	public Schueler findFirstBySgBpknrOrSgFamNameAndSgVorname1AndSgGeburtDat(@Param("sgBpknr") String sgBpknr,
			@Param("sgFamName") String sgFamName, @Param("sgVorname1") String sgVorname1,
			@Param("sgGeburtDat") LocalDate sgGeburtDat);

	public List<Schueler> findSchuelerByLaufbahn(@Param("suKennzahl") Integer suKennzahl,
			@Param("stichtag") LocalDate stichtag);

	public List<Schueler> findSchuelerByFamnameAndVornameAndDate(
	    @Param("famname") String famname,
		@Param("vorname") String vorname,
		@Param("years") int years,
		@Param("endDate") LocalDate endDate,
	    @Param("schoolIds") List<Integer> schoolIds);

	public List<Schueler> findSchuelerByLaufbahnAndVornameAndNachname(@Param("stichtag") LocalDate stichtag, @Param("vorname") String sgVorname1 ,
																	  @Param("famname") String sgFamName, @Param("suKennzahl") Integer suKennzahl);

	public List<Schueler> findSchuelerByLaufbahnAndVornameAndNachnameAndGroup(@Param("stichtag") LocalDate stichtag, @Param("vorname") String sgVorname1 ,
																	  @Param("famname") String sgFamName, @Param("suKennzahl") Integer suKennzahl, @Param("groupId") Long groupId);

	public List<Schueler> findSchuelerByLaufbahnAndVornameAndNachnameForCheckliste(@Param("stichtag") LocalDate stichtag, @Param("vorname") String sgVorname1 ,
																	  @Param("famname") String sgFamName, @Param("suKennzahl") Integer suKennzahl);

	public List<Schueler> findSchuelerByLaufbahnAndVornameAndNachnameAndGroupForCheckliste(@Param("stichtag") LocalDate stichtag, @Param("vorname") String sgVorname1 ,
																			  @Param("famname") String sgFamName, @Param("suKennzahl") Integer suKennzahl, @Param("groupId") Long groupId);

	public List<Schueler> findSchuelerByStichtagVornameNachnameAndGrName(
	    @Param("stichtag") LocalDate stichtag,
	    @Param("vorname") String sgVorname1 ,
	    @Param("famname") String sgFamName,
	    @Param("suKennzahl") Integer suKennzahl,
	    @Param("grName") String grName);

    public List<Schueler> findSchuelerByFamnameAndVornameAllwoLaufbahn(
      @Param("famname") String famname,
      @Param("vorname") String vorname,
      @Param("minAge") int minAge,
      @Param("maxAge") int maxAge,
      @Param("endDate") LocalDate endDate,
      @Param("schoolIds") List<Integer> schoolIds,
	  @Param("actualSuKennzahl") Integer actualSuKennzahl);

    public List<Schueler> findSchuelerByFamnameAndVornameAndDateWoLaufbahn(
      @Param("famname") String famname,
      @Param("vorname") String vorname,
      @Param("years") int years,
      @Param("endDate") LocalDate endDate,
      @Param("schoolIds") List<Integer> schoolIds);

	public Integer findCountByFamnameAndVornameAllwoLaufbahn(
      @Param("famname") String famname,
      @Param("vorname") String vorname,
      @Param("minAge") int minAge,
      @Param("maxAge") int maxAge,
      @Param("endDate") LocalDate endDate,
      @Param("schoolIds") List<Integer> schoolIds);
	
	public Integer findCountByFamnameAndVornameAndAgeWoLaufbahn(
	  @Param("famname") String famname,
	  @Param("vorname") String vorname,
	  @Param("years") int years,
	  @Param("endDate") LocalDate endDate,
      @Param("schoolIds") List<Integer> schoolIds);

	public Integer findCountByFamnameAndVornameAndSgGeburtDat(
			@Param("famname") String famname,
			@Param("vorname") String vorname,
			@Param("geburtdat") LocalDate geburtdat
	);

	public Schueler findByFamnameAndVornameAndSgGeburtDat(
			@Param("famname") String famname,
			@Param("vorname") String vorname,
			@Param("geburtdat") LocalDate geburtdat
	);

	public Integer findCountBySgSVNR(
			@Param("sgSVNR") String sgSVNR
	);

	public Schueler findBySgSVNR(
			@Param("sgSVNR") String sgSVNR
	);
}
