package bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import beans.*;

@Configuration
public class BeanConfiguration {
	
	@Bean
	public Item Item() {
		Item bean = new Item();
		return bean;
	}
	
	@Bean
	public Player Player() {
		Player bean = new Player();
		return bean;
	}

	@Bean
	public Server Server() {
		Server bean = new Server();
		return bean;
	}
	
	@Bean
	public Leaderboard Leaderboard() {
		Leaderboard bean = new Leaderboard();
		return bean;
	}
}
