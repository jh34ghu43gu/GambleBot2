package repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import beans.Leaderboard;


public interface LeaderboardRepository  extends JpaRepository<Leaderboard, String>{

	public Optional<List<Leaderboard>> findByEventOrderByValueAsc(String s);
}
