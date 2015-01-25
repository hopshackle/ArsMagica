package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

public class Spell {
	
	private long inventorId;
	private int yearInvented;
	private Arts technique;
	private Arts requisiteTechnique;
	private Arts form;
	private Arts requisiteForm;
	private int level;
	private String name;

	public Spell(Arts technique, Arts form, int level, String name, Agent inventor) {
		if (technique == null)
			throw new Error("Technique is null");
		if (form == null)
			throw new Error("Form is null");
		this.technique = technique;
		this.form = form;
		this.name = name;
		this.level = level;
		if (inventor != null) {
			yearInvented = inventor.getWorld().getYear();
			inventorId = inventor.getUniqueID();
		}
	}
	
	public long getInventorId() {return inventorId;}
	public Arts getTechnique() {return technique;}
	public Arts getForm() {return form;}
	public int getYearInvented() {return yearInvented;}
	public int getLevel() {return level;}
	public String getName() {return name;}
	
	@Override
	public String toString() {
		String retValue = String.format("%s%s %d  %s", technique.getAbbreviation(), form.getAbbreviation(), level, name);
		if (requisiteTechnique != null && requisiteForm == null)
			retValue = String.format("%s(%s)%s %d  %s", technique.getAbbreviation(), requisiteTechnique.getAbbreviation(),form.getAbbreviation(), level, name);
		if (requisiteTechnique == null && requisiteForm != null)
			retValue = String.format("%s%s(%s) %d  %s", technique.getAbbreviation(), form.getAbbreviation(), requisiteForm.getAbbreviation(), level, name);
		if (requisiteTechnique != null && requisiteForm != null) 
			retValue = String.format("%s(%s)%s(%s) %d  %s", technique.getAbbreviation(), requisiteTechnique.getAbbreviation(), 
					form.getAbbreviation(), requisiteForm.getAbbreviation(), level, name);
		return retValue;
	}

	public Arts getRequisiteForm() {
		return requisiteForm;
	}

	public void setRequisiteForm(Arts requisiteForm) {
		this.requisiteForm = requisiteForm;
	}

	public Arts getRequisiteTechnique() {
		return requisiteTechnique;
	}

	public void setRequisiteTechnique(Arts requisiteTechnique) {
		this.requisiteTechnique = requisiteTechnique;
	}	
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Spell))
			return false;
		Spell s = (Spell) o;
		return (s.getName() == getName() && s.getLevel() == getLevel());
	}
	
	@Override
	public int hashCode() {
		return level * 13 + name.hashCode();
	}
}
