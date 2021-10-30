package beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import bot.GambleBot;
import repository.PlayerRepository;

/**
 * A player or 'user' that owns items.
 * @author jh34ghu43gu
 */
@Entity
@Table(name="players")
public class Player {
	
	
	
	@Id
	@Column(name="player_id")
	private String id;
	@Column(name="player_balance")
	private double balance;
	@Column(name="player_st_dry")
	private int steelDry;
	@Column(name="player_me_dry")
	private int mechaDry;
	@Column(name="player_tc_dry")
	private int twoDry;
	@Column(name="player_gg_dry")
	private int gearDry;
	@Column(name="player_overall_dry")
	private int overallDry;
	@Column(name="player_pan_dry")
	private int panDry;
	@Column(name="player_os_tour")
	private int osTour;
	@Column(name="player_st_tour")
	private int stTour;
	@Column(name="player_me_tour")
	private int meTour;
	@Column(name="player_tc_tour")
	private int tcTour;
	@Column(name="player_gg_tour")
	private int ggTour;
	
	public Player() {
		super();
	}
	
	/**
	 * @param id
	 * @param balance
	 */
	public Player(String id, double balance) {
		super();
		this.id = id;
		this.balance = balance;
		steelDry = 0;
		mechaDry = 0;
		twoDry = 0;
		gearDry = 0;
		overallDry = 0;
		panDry = 0;
		osTour = 0;
		stTour = 0;
		meTour = 0;
		tcTour = 0;
		ggTour = 0;
	}
	
	/**
	 * Commit to database
	 */
	public void save() {
		GambleBot.getContext().getBean(PlayerRepository.class).save(this);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(double balance) {
		this.balance = balance;
	}

	/**
	 * @return the steelDry
	 */
	public int getSteelDry() {
		return steelDry;
	}

	/**
	 * @param steelDry the steelDry to set
	 */
	public void setSteelDry(int steelDry) {
		this.steelDry = steelDry;
	}

	/**
	 * @return the mechaDry
	 */
	public int getMechaDry() {
		return mechaDry;
	}

	/**
	 * @param mechaDry the mechaDry to set
	 */
	public void setMechaDry(int mechaDry) {
		this.mechaDry = mechaDry;
	}

	/**
	 * @return the twoDry
	 */
	public int getTwoDry() {
		return twoDry;
	}

	/**
	 * @param twoDry the twoDry to set
	 */
	public void setTwoDry(int twoDry) {
		this.twoDry = twoDry;
	}

	/**
	 * @return the gearDry
	 */
	public int getGearDry() {
		return gearDry;
	}

	/**
	 * @param gearDry the gearDry to set
	 */
	public void setGearDry(int gearDry) {
		this.gearDry = gearDry;
	}

	/**
	 * @return the overallDry
	 */
	public int getOverallDry() {
		return overallDry;
	}

	/**
	 * @param overallDry the overallDry to set
	 */
	public void setOverallDry(int overallDry) {
		this.overallDry = overallDry;
	}

	/**
	 * @return the panDry
	 */
	public int getPanDry() {
		return panDry;
	}

	/**
	 * @param panDry the panDry to set
	 */
	public void setPanDry(int panDry) {
		this.panDry = panDry;
	}

	/**
	 * @return the osTour
	 */
	public int getOsTour() {
		return osTour;
	}

	/**
	 * @param osTour the osTour to set
	 */
	public void setOsTour(int osTour) {
		this.osTour = osTour;
	}

	/**
	 * @return the stTour
	 */
	public int getStTour() {
		return stTour;
	}

	/**
	 * @param stTour the stTour to set
	 */
	public void setStTour(int stTour) {
		this.stTour = stTour;
	}

	/**
	 * @return the meTour
	 */
	public int getMeTour() {
		return meTour;
	}

	/**
	 * @param meTour the meTour to set
	 */
	public void setMeTour(int meTour) {
		this.meTour = meTour;
	}

	/**
	 * @return the tcTour
	 */
	public int getTcTour() {
		return tcTour;
	}

	/**
	 * @param tcTour the tcTour to set
	 */
	public void setTcTour(int tcTour) {
		this.tcTour = tcTour;
	}

	/**
	 * @return the ggTour
	 */
	public int getGgTour() {
		return ggTour;
	}

	/**
	 * @param ggTour the ggTour to set
	 */
	public void setGgTour(int ggTour) {
		this.ggTour = ggTour;
	}

	
}
