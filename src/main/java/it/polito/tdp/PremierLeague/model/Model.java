package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	PremierLeagueDAO dao;
	private Graph<Team, DefaultWeightedEdge> grafo;
	private Map <Integer, Team> idMap;
	private List<TeamClassifica> squadremigliori;
	private List<TeamClassifica> squadrepeggiori;
	
	private double media;
	private int inferiore;
	
	public Model() {
		this.dao = new PremierLeagueDAO();
		this.idMap = new HashMap<>();
	}
	
	public String creaGrafo() {
		grafo = new SimpleDirectedWeightedGraph <> (DefaultWeightedEdge.class);
		
		//Aggiunta vertici
		Graphs.addAllVertices(this.grafo, this.dao.listAllTeams());
		for (Team t : grafo.vertexSet())
			idMap.put(t.getTeamID(), t);
		
		//Aggiunta archi
		for(TeamClassifica tc1: this.calcolaClassifica()) {
			for(TeamClassifica tc2: this.calcolaClassifica()) {
				if(!tc1.equals(tc2) && tc1.getPunteggio()!=tc2.getPunteggio()) {
					if(tc1.getPunteggio()>tc2.getPunteggio())
						Graphs.addEdgeWithVertices(this.grafo, tc1.getSquadra(), tc2.getSquadra(), tc1.getPunteggio()-tc2.getPunteggio());
					else if(tc1.getPunteggio()<tc2.getPunteggio())
						Graphs.addEdgeWithVertices(this.grafo, tc2.getSquadra(), tc1.getSquadra(), tc2.getPunteggio()-tc1.getPunteggio());
				}			
			}
		}	
		
		media = 0.0;
		inferiore = 0;
		
		return String.format("Grafo creato con %d vertici e %d archi\n", 
				this.grafo.vertexSet().size(), this.grafo.edgeSet().size());
	}
	
	public Graph<Team, DefaultWeightedEdge> getGrafo() {
		return grafo;
	}

	public List<TeamClassifica> calcolaClassifica() {
		List <TeamClassifica> classifica = new ArrayList<>();

		for(Team t : grafo.vertexSet()) {
			TeamClassifica team = new TeamClassifica(t, 0);
			classifica.add(team);
		}		
				
		
		for(Match m : this.dao.listAllMatches()) {
			if(m.resultOfTeamHome == 1) {
			  for(TeamClassifica tc : classifica ) {
				if(m.teamHomeNAME.equals(tc.getSquadra().getName()))
					tc.setPunteggio(tc.getPunteggio()+3);
			  }
			}
			
			else if(m.resultOfTeamHome == -1) {
			  for(TeamClassifica tc : classifica ) {
				 if(m.teamAwayNAME.equals(tc.getSquadra().getName()))
				    tc.setPunteggio(tc.getPunteggio()+3);
			  }
		    }
			
			else if(m.resultOfTeamHome == 0) {
			  for(TeamClassifica tc : classifica ) {
				 if(m.teamHomeNAME.equals(tc.getSquadra().getName()))
						tc.setPunteggio(tc.getPunteggio()+1);
				 if(m.teamAwayNAME.equals(tc.getSquadra().getName()))
					 tc.setPunteggio(tc.getPunteggio()+1);
			  }
			}
		}
		
		Collections.sort(classifica);
		return classifica;
	}
	
	public List<String> getTeams() {
		List<String> result = new ArrayList<>();
		for(Team t : this.dao.listAllTeams()) {
			result.add(t.getName());
		}
		
		Collections.sort(result);
		
		return result;
	}
	
	public void squadreMigliori(String team) {
		this.squadremigliori = new ArrayList<>();
		int differenzaPunti;
		TeamClassifica squadra = null;
		
		for(TeamClassifica tc : this.calcolaClassifica()) {
			if(tc.getSquadra().getName().equals(team))
			     squadra = tc;
		}
		
		for(TeamClassifica tc : this.calcolaClassifica()) {
			if(tc.getPunteggio() > squadra.getPunteggio()) {
				differenzaPunti = tc.getPunteggio() - squadra.getPunteggio();
				squadremigliori.add(new TeamClassifica(tc.getSquadra(), differenzaPunti));
		    }
		}
		
	}
	
	public String stampaSquadreMigliori() {
		String result = "";
		
		for(TeamClassifica tc : this.squadremigliori)
			result+=tc.toString();
		
		return result;
	}
	
	public void squadrePeggiori(String team) {
		this.squadrepeggiori = new ArrayList<>();
		int differenzaPunti;
		TeamClassifica squadra = null;
		
		for(TeamClassifica tc : this.calcolaClassifica()) {
			if(tc.getSquadra().getName().equals(team))
			     squadra = tc;
		}
		
		for(TeamClassifica tc : this.calcolaClassifica()) {
			if(tc.getPunteggio() < squadra.getPunteggio()) {
				differenzaPunti = squadra.getPunteggio() - tc.getPunteggio();
				squadrepeggiori.add(new TeamClassifica(tc.getSquadra(), differenzaPunti));
		    }
		}
		
	}
	
	public String stampaSquadrePeggiori() {
		String result = "";
		
		for(TeamClassifica tc : this.squadrepeggiori)
			result+=tc.toString();
		
		return result;
	}

	public void simula(int n, int x) {
		Simulatore s = new Simulatore(this.dao.listAllMatches(),n, x, idMap);
		inferiore = s.getInferiori();
		media = s.getMedia();
		
		
	}

	public List<TeamClassifica> getSquadremigliori() {
		return squadremigliori;
	}

	public List<TeamClassifica> getSquadrepeggiori() {
		return squadrepeggiori;
	}

	public double getMedia() {
		return media;
	}

	public int getInferiore() {
		return inferiore;
	}
	
	
	
	
	
}
