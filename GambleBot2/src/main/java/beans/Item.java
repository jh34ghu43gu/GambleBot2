package beans;

import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import bot.GambleBot;
import files.ConfigHelper;
import model.Crate;
import repository.ItemRepository;


/**
 * The item class to represent an item. Must have a name, quality, level, quantity, craftable, and owner. 
 * Has an item id auto-incrementing primary key.
 * <p>
 * Killstreak tier, festive status, and value are not required when creating but not-null in DB with defaults 0
 * <p>
 * Optional fields: secondary quality, killstreak sheen, killstreaker, paint, effect, tier, wear, origin, and inputs
 * Note: Inputs are stored as a 30 bit int
 * @author jh34ghu43gu
 */
@Entity
@Table(name="items")
public class Item {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="item_id")
	private long id;
	@Column(name="item_name")
	private String name;
	@Column(name="item_quality")
	private String quality;
	@Column(name="item_secondary_quality")
	private String secondaryQuality;
	@Column(name="item_level")
	private int level;
	@Column(name="item_quantity")
	private int quantity;
	@ManyToOne(fetch=FetchType.LAZY, optional = false)
	@JoinColumn(name="item_owner")
	private Player owner;
	@Column(name="item_killstreak_tier")
	private int killstreakTier;
	@Column(name="item_killstreak_sheen")
	private String killstreakSheen;
	@Column(name="item_killstreaker")
	private String killstreaker;
	@Column(name="item_paint")
	private String paint;
	@Column(name="item_inputs")
	private int inputs;
	@Column(name="item_festive")
	private boolean festive;
	@Column(name="item_value")
	private double value;
	@Column(name="item_effect")
	private String effect;
	@Column(name="item_craftable")
	private boolean craftable;
	@Column(name="item_wear")
	private String wear;
	@Column(name="item_tier")
	private String tier;
	@Column(name="item_origin")
	private String origin;
	
	public Item() {
		super();
	}
	
	/**
	 * Bare-bones Item 
	 * @param name
	 * @param quality
	 * @param level
	 * @param quantity
	 * @param owner
	 * @param craftable
	 */
	public Item(String name, String quality, int level, int quantity, Player owner, Boolean craftable) {
		super();
		this.name = name;
		this.quality = quality;
		this.level = level;
		this.quantity = quantity;
		this.owner = owner;
		this.craftable = craftable;
	}

	/**
	 * All non-null fields
	 * @param name
	 * @param quality
	 * @param level
	 * @param quantity
	 * @param owner
	 * @param killstreakTier	0 = none, 1 = normal, 2 = specialized, 3 = professional
	 * @param festive
	 * @param value
	 * @param craftable
	 */
	public Item(String name, String quality, int level, int quantity, Player owner, int killstreakTier, boolean festive,
			double value, boolean craftable) {
		super();
		this.name = name;
		this.quality = quality;
		this.level = level;
		this.quantity = quantity;
		this.owner = owner;
		this.killstreakTier = killstreakTier;
		this.festive = festive;
		this.value = value;
		this.craftable = craftable;
	}

	/**
	 * Bare-bones killstreak item. Passing empty strings for sheen or KSer will leave entry null in DB
	 * @param name
	 * @param quality
	 * @param level
	 * @param quantity
	 * @param owner
	 * @param killstreakTier	0 = none, 1 = normal, 2 = specialized, 3 = professional
	 * @param killstreakSheen
	 * @param killstreaker
	 * @param craftable
	 */
	public Item(String name, String quality, int level, int quantity, Player owner, int killstreakTier,
			String killstreakSheen, String killstreaker, boolean craftable) {
		super();
		this.name = name;
		this.quality = quality;
		this.level = level;
		this.quantity = quantity;
		this.owner = owner;
		this.killstreakTier = killstreakTier;
		if(!killstreakSheen.isEmpty()) {
			this.killstreakSheen = killstreakSheen;
		}
		if(!killstreaker.isEmpty()) {
			this.killstreaker = killstreaker;
		}
		this.craftable = craftable;
	}
	
	/**
	 * Graded item
	 * @param name
	 * @param quality
	 * @param level
	 * @param quantity
	 * @param owner
	 * @param craftable
	 * @param tier
	 */
	public Item(String name, String quality, int level, int quantity, Player owner, boolean craftable, String tier) {
		super();
		this.name = name;
		this.quality = quality;
		this.level = level;
		this.quantity = quantity;
		this.owner = owner;
		this.craftable = craftable;
		this.tier = tier;
	}
	
	/**
	 * War-paint or skinned item
	 * @param name
	 * @param quality
	 * @param level
	 * @param quantity
	 * @param owner
	 * @param craftable
	 * @param tier
	 * @param wear
	 */
	public Item(String name, String quality, int level, int quantity, Player owner, boolean craftable, String tier,
			String wear) {
		super();
		this.name = name;
		this.quality = quality;
		this.level = level;
		this.quantity = quantity;
		this.owner = owner;
		this.craftable = craftable;
		this.wear = wear;
		this.tier = tier;
	}
	
	public boolean canCombine(Item item) {
		boolean canCombine = true;
		//Check non-nulls are matching
		if(!name.equals(item.getName()) || //Name match
				level != item.getLevel() || //Same levels
				!quality.equals(item.getQuality()) || //Same quality
				!owner.getId().equals(item.owner.getId()) || //Same owner
				killstreakTier != item.getKillstreakTier() || //Same killstreak tier
				craftable != item.isCraftable() || //Same craftable status
				festive != item.isFestive()) { //Same festive type
			canCombine = false;
		}
		
		//Secondary quality
		if(secondaryQuality != null) {
			if(item.getSecondaryQuality() != null) {
				if(!secondaryQuality.equals(item.getSecondaryQuality())) {
					canCombine = false; //We both have 2nd quality but they are different
				}
			} else { 
				canCombine = false; //we have 2nd quality, they do not
			}
		} else {
			if(item.getSecondaryQuality() != null) {
				canCombine = false; //We don't have 2nd quality, they do
			}
		}
		
		//Check matching ks combos
		if(killstreakTier >= 2) { //Sheens for spec or prof
			if(!killstreakSheen.equals(item.getKillstreakSheen())) {
				canCombine = false;
			}
			if(killstreakTier == 3) {
				if(!killstreaker.equals(item.getKillstreaker())) {
					canCombine = false;
				}
			}
		}
		
		//Wears
		String w1 = "";
		String w2 = "";
		try {
			if(wear.length() > 0) {
				w1 = wear;
			}
			if(item.getWear().length() > 0) {
				w2 = item.getWear();
			}
		} catch(NullPointerException e) {
			
		}
		if(w1.length() > 0 && w2.length() > 0) { //Check if both got assigned
			if(!w1.equals(w2)) { //If they aren't the same can't combine
				canCombine = false;
			}
		} else if(w1.length() != w2.length()){ //If only one got assigned then can't combine
			canCombine = false;
		}
			
		return canCombine;
	}
	
	@Override
	public String toString() {
		String out = "" + quantity;
		if(secondaryQuality != null) {
			out += " " + secondaryQuality;
		}
		out += " " + quality + " " + name + ", Level: " + level + ", Origin: " + origin;
		return out;
	}
	
	/**
	 * @return A discord friendly string
	 */
	public String toDiscordString() {
		String out = "" + quantity;
		//Amount followed by secondary quality (strange)
		if(secondaryQuality != null) {
			out += " " + secondaryQuality;
		}
		//Primary quality unless it's unusual
		if(!quality.equals("Unusual")){
			out += " " + quality;
		} else if(effect != null) { //If it's unusual put the effect instead, if it's not null (fallback to quality if null effect)
			out += " " + effect;
		} else {
			out += " " + quality;
		}
		
		//Killstreak title
		if(this.killstreakTier == 1) {
			out += " Killstreak";
		} else if(this.killstreakTier == 2) {
			out += " Specialized killstreak";
		} else if(this.killstreakTier == 3) {
			out += " Professional killstreak";
		}
		
		//Name
		out += " " + name;
		
		//Wear
		if(wear != null){
			out += " (" + wear + ")";
		}
		
		//Killstreak effects
		if(this.killstreakTier == 2) {
			out += " (" + this.killstreakSheen + ")";
		} else if(this.killstreakTier == 3) {
			out += " (" + this.killstreaker + ", " + this.killstreakSheen + ")";
		}
		
		//Include level for pans
		if(this.name.contains("Golden Frying")) {
			out += " Level: " + this.level;
		}
		
		//Tier orb
		if(tier != null) {
			if(Crate.rarityEmotes.containsKey(tier)) {
				out += " " + Crate.rarityEmotes.get(tier);
			}
		}
		
		//Strange orb
		if(quality.equals("Strange") || (secondaryQuality != null && secondaryQuality.equals("Strange"))) {
			out += " " + ConfigHelper.getOptionFromFile("STRANGE_EMOTE");
		}
		return out;
	}
	
	/**
	 * Commit to database
	 */
	public void save() {
		ItemRepository IR = GambleBot.getContext().getBean(ItemRepository.class);
		Optional<List<Item>> items = IR.findByOwnerAndName(this.owner, this.name);
		//If player already has this item try to combine it.
		if(items.isPresent()) {
			List<Item> itemList = items.get();
			for(Item i : itemList) {
				if(i.canCombine(this)) {
					i.setQuantity(i.getQuantity() + this.getQuantity());
					IR.save(i);
					return; //Don't add to any others that may exist
				}
			}
			//Didn't find a true duplicate we could combine with, make our own
			IR.save(this);
			return;
		} else {
			IR.save(this);
		}
	}

	//Generic getters and setters
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the quality
	 */
	public String getQuality() {
		return quality;
	}

	/**
	 * @param quality the quality to set
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}

	/**
	 * @return the secondaryQuality
	 */
	public String getSecondaryQuality() {
		return secondaryQuality;
	}

	/**
	 * @param secondaryQuality the secondaryQuality to set
	 */
	public void setSecondaryQuality(String secondaryQuality) {
		this.secondaryQuality = secondaryQuality;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
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
	 * @return the killstreakTier 0 is none, 1 is normal, 2 is spec, 3 is pro
	 */
	public int getKillstreakTier() {
		return killstreakTier;
	}

	/**
	 * @param killstreakTier	0 is none, 1 is normal, 2 is spec, 3 is pro
	 */
	public void setKillstreakTier(int killstreakTier) {
		this.killstreakTier = killstreakTier;
	}

	/**
	 * @return the killstreakSheen
	 */
	public String getKillstreakSheen() {
		return killstreakSheen;
	}

	/**
	 * @param killstreakSheen the killstreakSheen to set
	 */
	public void setKillstreakSheen(String killstreakSheen) {
		this.killstreakSheen = killstreakSheen;
	}

	/**
	 * @return the killstreaker
	 */
	public String getKillstreaker() {
		return killstreaker;
	}

	/**
	 * @param killstreaker the killstreaker to set
	 */
	public void setKillstreaker(String killstreaker) {
		this.killstreaker = killstreaker;
	}

	/**
	 * @return the paint
	 */
	public String getPaint() {
		return paint;
	}

	/**
	 * @param paint the paint to set
	 */
	public void setPaint(String paint) {
		this.paint = paint;
	}

	/**
	 * @return the inputs
	 */
	public int getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(int inputs) {
		this.inputs = inputs;
	}

	/**
	 * @returnIs the item festivized
	 */
	public boolean isFestive() {
		return festive;
	}

	/**
	 * @param festive	
	 */
	public void setFestive(boolean festive) {
		this.festive = festive;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the effect
	 */
	public String getEffect() {
		return effect;
	}

	/**
	 * @param effect the effect to set
	 */
	public void setEffect(String effect) {
		this.effect = effect;
	}

	/**
	 * @return the craftable
	 */
	public boolean isCraftable() {
		return craftable;
	}

	/**
	 * @param craftable the craftable to set
	 */
	public void setCraftable(boolean craftable) {
		this.craftable = craftable;
	}

	/**
	 * @return the wear
	 */
	public String getWear() {
		return wear;
	}

	/**
	 * @param wear the wear to set
	 */
	public void setWear(String wear) {
		this.wear = wear;
	}

	/**
	 * @return the tier
	 */
	public String getTier() {
		return tier;
	}

	/**
	 * @param tier the tier to set
	 */
	public void setTier(String tier) {
		this.tier = tier;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	
}
