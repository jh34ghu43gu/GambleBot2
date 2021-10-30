package repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import beans.Server;

@Repository
public interface ServerRepository extends JpaRepository<Server, String>{

	public Optional<Server> findById(String s);
	
}
