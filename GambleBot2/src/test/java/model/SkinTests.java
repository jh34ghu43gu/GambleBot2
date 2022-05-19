package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class SkinTests {
	
	@Test
	public void compareTest() {
		String[] tiers = {"Civilian", "Freelance", "Mercenary", "Commando", "Assassin", "Elite"};
		
		Skin comp = new Skin();
		
		Skin tiger1 = new Skin("tiger", tiers[2], false, false, "");
		Skin tiger2 = new Skin("tiger", tiers[2], false, false, "");
		Skin Stiger1 = new Skin("tiger", tiers[2], true, false, "");
		Skin Stiger2 = new Skin("tiger", tiers[2], true, false, "");
		Skin Utiger1 = new Skin("tiger", tiers[2], false, true, "Hot");
		Skin Utiger2 = new Skin("tiger", tiers[2], false, true, "Hot");
		Skin SUtiger1 = new Skin("tiger", tiers[2], true, true, "Hot");
		Skin SUtiger2 = new Skin("tiger", tiers[2], true, true, "Hot");
		
		Skin bank1 = new Skin("bank", tiers[3], false, false, "");
		Skin Sbank1 = new Skin("bank", tiers[3], true, false, "");
		Skin Ubank1 = new Skin("bank", tiers[3], false, true, "Hot");
		Skin SUbank1 = new Skin("bank", tiers[3], true, true, "Hot");
		
		//Note all values inverted from the comments for sorting purposes
		//Same skins
		assertEquals(comp.compare(tiger1, tiger2), 0);
		assertEquals(comp.compare(Stiger1, Stiger2), 0);
		assertEquals(comp.compare(Utiger1, Utiger2), 0);
		assertEquals(comp.compare(SUtiger1, SUtiger2), 0);
		
		//S, U, SU > non
		assertEquals(comp.compare(Stiger1, tiger1), -1);
		assertEquals(comp.compare(Utiger1, tiger1), -1);
		assertEquals(comp.compare(SUtiger1, tiger1), -1);
		
		//non < S, U, SU
		assertEquals(comp.compare(tiger1, Stiger1), 1);
		assertEquals(comp.compare(tiger1, Utiger1), 1);
		assertEquals(comp.compare(tiger1, SUtiger1), 1);
		
		// SU > U > S
		assertEquals(comp.compare(Utiger1, Stiger1), -1);
		assertEquals(comp.compare(SUtiger1, Stiger1), -1);
		assertEquals(comp.compare(SUtiger1, Utiger1), -1);
		
		// S < U < SU
		assertEquals(comp.compare(Stiger1, Utiger1), 1);
		assertEquals(comp.compare(Stiger1, SUtiger1), 1);
		assertEquals(comp.compare(Utiger1, SUtiger1), 1);
		
		// commando > merc
		assertEquals(comp.compare(bank1, tiger1), -1);
		assertEquals(comp.compare(Sbank1, Stiger1), -1);
		assertEquals(comp.compare(Ubank1, Utiger1), -1);
		assertEquals(comp.compare(SUbank1, SUtiger1), -1);
		
		// merc < commando
		assertEquals(comp.compare(tiger1, bank1), 1);
		assertEquals(comp.compare(Stiger1, Sbank1), 1);
		assertEquals(comp.compare(Utiger1, Ubank1), 1);
		assertEquals(comp.compare(SUtiger1, SUbank1), 1);
	}

}
