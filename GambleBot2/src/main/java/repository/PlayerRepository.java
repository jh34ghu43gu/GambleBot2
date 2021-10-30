package repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import beans.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String>{

	public Optional<Player> findById(String s);
}
