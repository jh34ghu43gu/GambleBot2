package beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import bot.GambleBot;
import repository.ServerRepository;


/**
 * Represents a discord server and stores that server's settings.
 * @author jh34ghu43gu
 */
@Entity
@Table(name="servers")
public class Server {
	
	@Id
	@Column(name="server_id")
	private String id;
	@Column(name="server_mvm_channel")
	private String mvmChannel;
	@Column(name="server_crate_channel")
	private String crateChannel;
	
	public Server() {
		super();
	}
	
	public Server(String id) {
		super();
		this.id = id;
		this.mvmChannel = "";
		this.crateChannel = "";
	}
	
	/**
	 * Commit to database
	 */
	public void save() {
		GambleBot.getContext().getBean(ServerRepository.class).save(this);
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
	 * @return the mvmChannel
	 */
	public String getMvmChannel() {
		return mvmChannel;
	}

	/**
	 * @param mvmChannel the mvmChannel to set
	 */
	public void setMvmChannel(String mvmChannel) {
		this.mvmChannel = mvmChannel;
	}

	/**
	 * @return the crateChannel
	 */
	public String getCrateChannel() {
		return crateChannel;
	}

	/**
	 * @param crateChannel the crateChannel to set
	 */
	public void setCrateChannel(String crateChannel) {
		this.crateChannel = crateChannel;
	}
	
}
