package beans;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Id;

import bot.GambleBot;
import repository.LeaderboardRepository;

@Entity
@Table(name="leaderboard")
public class Leaderboard {

	@Column(name="event")
	private String event;
	@Column(name="value")
	private int value;
	@Column(name="date")
	private Timestamp date;
	@ManyToOne(fetch=FetchType.LAZY, optional = false)
	@JoinColumn(name="owner")
	private Player owner;
	@Id
	@Column(name="event_id")
	private int id;
	
	public Leaderboard() {
		super();
	}
	
	public Leaderboard(String event, int value, Timestamp date, Player owner) {
		super();
		this.event = event;
		this.value = value;
		this.date = date;
		this.owner = owner;
	}

	/**
	 * Commit to database
	 */
	public void save() {
		GambleBot.getContext().getBean(LeaderboardRepository.class).save(this);
	}
	
	/**
	 * Delete from database
	 */
	public void delete() {
		GambleBot.getContext().getBean(LeaderboardRepository.class).delete(this);
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}
	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
	/**
	 * @return the date
	 */
	public Timestamp getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(Timestamp date) {
		this.date = date;
	}
	/**
	 * @return the owner
	 */
	public Player getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Player owner) {
		this.owner = owner;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	
}
