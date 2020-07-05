package hopshackle.simulation.arsmagica;

public enum HermeticHouse {

    BONISAGUS,
    TRIANOMA,
    FLAMBEAU,
    TREMERE,
    TYTALUS,
    VERDITIUS,
    DIEDNE,
    BJORNAER,
    CRIAMON,
    JERBITON,
    MERINITA,
    MERCERE,
    GUERNICUS;

    private int apprenticeshipModifier;

    public void updateApprenticeshipModifier(int membership, int totalMagi) {
        double meanPerHouse = totalMagi / 12.0;
        double popMod = Math.min(0.0, (700.0 - totalMagi)) / 35.0;
        switch (this) {
            case TRIANOMA:
            case MERCERE:
                meanPerHouse /= 2.0;
            default:
        }

        double relativeScarcity = membership / meanPerHouse;
        apprenticeshipModifier = (int) (popMod + (relativeScarcity < -.01 ? 100.0 : (1.0 - relativeScarcity) * 5.0));
/*
        if (apprenticeshipModifier != 0)
            System.out.println(String.format("%+d Modifier for %s", apprenticeshipModifier, this.toString()));
*/
    }

    public int getApprenticeshipModifier() {
        return apprenticeshipModifier;
    }

}
