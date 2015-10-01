package shared;

/**
 * rock code is the USGS code for the lithologic pattern, rock name is the name
 * of the lithologic pattern, imagePath is to access the images of the
 * lithologic pattern.
 */
public class LithologicType {

    private int    rockCode;
    private String fullName, shortName, imagePath;

    private static final String[] abbrNames = { "Grv or Cgl (1)",
            "Grv or Cgl (2)", "x-bd Grv or Cgl", "", "Brec (1)", "Brec (2)",
            "Mass Sd. or Sst", "Bd Sd. or Sst", "x-bd Sd. or Sst (1)",
            "x-bd Sd. or Sst(2)", "Rpl-Bd Sd. or Sst", "arg or sh Sst",
            "calc Sst", "dol Sst", "", "Slt, Sltst, or sh Slt", "calc Sltst",
            "dol Sltst", "sdy or slty Sh", "Cl or Cl Sh", "cht Sh", "dol Sh",
            "calc Sh or marl", "carb Sh", "o Sh", "Chk", "Ls", "clas Ls",
            "foss clas Ls", "nod or irr Bd Ls",
            "Ls, irr (Bur?) fillings of sacc Dol", "x-bd Ls", "cht x-bd Ls",
            "cht and sdy x-bd clas Ls", "ooc Ls", "sdy Ls", "slty Ls",
            "arg or sh Ls", "cht Ls (1)", "cht Ls (2)", "dol Ls or lmy Dol",
            "Dolst or Dol", "x-bd Dolst or Dol", "ooc Dolst or Dol",
            "sdy Dolst or Dol", "slty Dolst or Dol", "arg or sh Dolst or Dol",
            "cht Dolst or Dol", "Bd Cht (1)", "Bd Cht (2)", "foss Bd Cht",
            "foss Rk", "Diatomaceous Rk", "Sbgwke", "x-bd Sbgwke",
            "Rpl-Bd Sbgwke", "Peat", "C", "Bony C or impure C", "Uc",
            "Flint Cl", "Bent", "Blauc", "Lim", "Sid.", "phos-nod Rk", "Gyp",
            "Sa", "intbd Sst and Sltst", "intbd Sst and Sh",
            "intbd RplBd Sst and Sh", "intbd Sh and slty Ls (Sh dom)",
            "intbd Sh and Ls (Sh dom) (1)", "intbd Sh and Ls (Sh dom) (2)",
            "intbd calc Sh and Ls (Sh dom)", "intbd slty Ls and Sh",
            "intbd Ls and Sh (1)", "intbd Ls and Sh (2)",
            "intbd Ls and Sh (Ls dom)", "intbd Ls and calc Sh",
            "Till or diamicton (1)", "Till or diamicton (2)",
            "Till or diamicton (3)", "Loess (1)", "Loess (2)", "Loess (3)", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "Meta", "Qtzt.",
            "Sl.", "sch or gns Grt", "Sch.", "cntrt Sch.", "Sch. and Gns",
            "Gns", "cntrt Gns", "Soapstone, talc, or serpentinite", "tf Rk",
            "Xl Tf", "devit Tf", "volc Brec and Tf", "volc Brec or Aglm",
            "Zeolitic Rk", "bas flows", "Grt (1)", "Grt (2)", "bnd Ig",
            "Ig (1)", "Ig (2)", "Ig (3)", "Ig (4)", "Ig (5)", "Ig (6)",
            "Ig (7)", "Ig (8)", "Porphyritic Rk (1)", "Porphyritic Rk (2)",
            "Vit", "Qtz.", "Ore" };
    private static final String[] fullNames = {
            "Gravel or conglomerate (1st option)",
            "Gravel or conglomerate (2nd option)",
            "Crossbedded gravel or conglomerate", "", "Breccia (1st option)",
            "Breccia (2nd option)", "Massive sand or sandstone",
            "Bedded sand or sandstone",
            "Crossbedded sand or sandstone (1st option)",
            "Crossbedded sand or sandstone(2nd option)",
            "Ripple-bedded sand or sandstone",
            "Argillaceous or shaly sandstone", "Calcareous sandstone",
            "Dolomitic sandstone", "Silt, siltstone, or shaly silt",
            "Calcareous siltstone", "Dolomitic siltstone", "",
            "Sandy or silty shale", "Clay or clay shale", "Cherty shale",
            "Dolomitic shale", "Calcareous shale or marl", "Carbonaceous shale",
            "Oil shale", "Chalk", "Limestone", "Clastic limestone",
            "Fossiliferous clastic limestone",
            "Nodular or irregularly bedded limestone",
            "Limestone, irregular (burrow?) fillings of saccharoidal dolomite",
            "Crossbedded limestone", "Cherty crossbedded limestone",
            "Cherty and sandy crossbedded clastic limestone",
            "Oolitic limestone", "Sandy limestone", "Silty limestone",
            "Argillaceous or shaly limestone", "Cherty limestone (1st option)",
            "Cherty limestone (2nd option)",
            "Dolomitic limestone,limy dolostone, or limy dolomite",
            "Dolostone or dolomite", "Crossbedded dolostone ordolomite",
            "Oolitic dolostone or dolomite", "Sandy dolostone or dolomite",
            "Silty dolostone or dolomite",
            "Argillaceous or shaly dolostone or dolomite",
            "Cherty dolostone or dolomite", "Bedded chert (1st option)",
            "Bedded chert (2nd option)", "Fossiliferous bedded chert",
            "Fossiliferous rock", "Diatomaceous rock", "Subgraywacke",
            "Crossbedded subgraywacke", "Ripple-bedded subgraywacke", "Peat",
            "Coal", "Bony coal or impure coal", "Underclay", "Flint clay",
            "Bentonite", "Glauconite", "Limonite", "Siderite",
            "Phosphatic-nodular rock", "Gypsum", "Salt",
            "Interbedded sandstone and siltstone",
            "Interbedded sandstone and shale",
            "Interbedded ripplebedded sandstone and shale",
            "Interbedded shale and silty limestone (shale dominant)",
            "Interbedded shale and limestone (shale dominant) (1st option)",
            "Interbedded shale and limestone (shale dominant) (2nd option)",
            "Interbedded calcareous shale and limestone (shale dominant)",
            "Interbedded silty limestone and shale",
            "Interbeddedlimestone and shale (1st option)",
            "Interbedded limestone and shale (2nd option)",
            "Interbedded limestone and shale (limestone dominant)",
            "Interbedded limestone and calcareous shale",
            "Till or diamicton (1st option)", "Till or diamicton (2nd option)",
            "Till or diamicton (3rd option)", "Loess (1st option)",
            "Loess (2nd option)", "Loess (3rd option)", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "Metamorphism", "Quartzite",
            "Slate", "Schistose or gneissoid granite", "Schist",
            "Contorted schist", "Schist and gneiss", "Gneiss",
            "Contorted gneiss", "Soapstone, talc, or serpentinite",
            "Tuffaceous rock", "Crystal tuff", "Devitrified tuff",
            "Volcanic breccia and tuff", "Volcanic breccia or agglomerate",
            "Zeolitic rock", "Basaltic flows", "Granite (1st option)",
            "Granite (2nd option)", "Banded igneous rock",
            "Igneous rock (1st option)", "Igneous rock (2nd option)",
            "Igneous rock (3rd option)", "Igneous rock (4th option)",
            "Igneous rock (5th option)", "Igneous rock (6th option)",
            "Igneous rock (7th option)", "Igneous rock (8th option)",
            "Porphyritic rock (1st option)", "Porphyritic rock (2nd option)",
            "Vitrophyre", "Quartz", "Ore" };

    public LithologicType(int rockCode) {
        this.rockCode = rockCode;
        fullName = fullNames[rockCode - 601];
        shortName = abbrNames[rockCode - 601];
        imagePath = "/client/images/lithPatterns/" + rockCode + ".gif";
    }

    /**
     * returns USGS rockcode
     */
    public int getRockCode() {
        return rockCode;
    }

    /**
     * returns full name of the rock name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * returns the abbreviated version of the rock name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * returns the imagePath
     */
    public String imagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return "" + getRockCode();
    }

    public static String[] toArrayString(LithologicType[] lithRocks) {
        String[] names = new String[lithRocks.length];
        for (int i = 0; i < lithRocks.length; i++)
            names[i] = lithRocks[i].getShortName();
        return names;
    }

}
