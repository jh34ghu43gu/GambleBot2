package repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import beans.Item;
import beans.Player;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer>{

	public Item findById(int i);
	public List<Item> findByOwner(Player id);
	public Optional<List<Item>> findByOwnerAndName(Player id, String name);
	public Optional<List<Item>> findByOwnerAndQuality(Player id, String quality);
	public Optional<List<Item>> findByOwnerAndOrigin(Player id, String origin);
	
}
