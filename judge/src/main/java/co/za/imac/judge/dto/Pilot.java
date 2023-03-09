package co.za.imac.judge.dto;

public class Pilot {
    private String freestyle;

    private String comments;

    private String addr2;

    private String addr1;

    private Classes classes;

    private String index;

    private String active;

    private String comp_id;

    private String frequency;

    private String spread_spectrum;

    private String secondary_id;

    private String airplane;

    private String name;

    private String missing_pilot_panel;

    private String primary_id;

    public String getFreestyle ()
    {
        return freestyle;
    }

    public void setFreestyle (String freestyle)
    {
        this.freestyle = freestyle;
    }

    public String getComments ()
    {
        return comments;
    }

    public void setComments (String comments)
    {
        this.comments = comments;
    }

    public String getAddr2 ()
    {
        return addr2;
    }

    public void setAddr2 (String addr2)
    {
        this.addr2 = addr2;
    }

    public String getAddr1 ()
    {
        return addr1;
    }

    public void setAddr1 (String addr1)
    {
        this.addr1 = addr1;
    }

    public Classes getClasses ()
    {
        return classes;
    }

    public void setClasses (Classes classes)
    {
        this.classes = classes;
    }

    public String getIndex ()
    {
        return index;
    }

    public void setIndex (String index)
    {
        this.index = index;
    }

    public String getActive ()
    {
        return active;
    }

    public void setActive (String active)
    {
        this.active = active;
    }

    public String getComp_id ()
    {
        return comp_id;
    }

    public void setComp_id (String comp_id)
    {
        this.comp_id = comp_id;
    }

    public String getFrequency ()
    {
        return frequency;
    }

    public void setFrequency (String frequency)
    {
        this.frequency = frequency;
    }

    public String getSpread_spectrum ()
    {
        return spread_spectrum;
    }

    public void setSpread_spectrum (String spread_spectrum)
    {
        this.spread_spectrum = spread_spectrum;
    }

    public String getSecondary_id ()
    {
        return secondary_id;
    }

    public void setSecondary_id (String secondary_id)
    {
        this.secondary_id = secondary_id;
    }

    public String getAirplane ()
    {
        return airplane;
    }

    public void setAirplane (String airplane)
    {
        this.airplane = airplane;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getMissing_pilot_panel ()
    {
        return missing_pilot_panel;
    }

    public void setMissing_pilot_panel (String missing_pilot_panel)
    {
        this.missing_pilot_panel = missing_pilot_panel;
    }

    public String getPrimary_id ()
    {
        return primary_id;
    }

    public void setPrimary_id (String primary_id)
    {
        this.primary_id = primary_id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [freestyle = "+freestyle+", comments = "+comments+", addr2 = "+addr2+", addr1 = "+addr1+", classes = "+classes+", index = "+index+", active = "+active+", comp_id = "+comp_id+", frequency = "+frequency+", spread_spectrum = "+spread_spectrum+", secondary_id = "+secondary_id+", airplane = "+airplane+", name = "+name+", missing_pilot_panel = "+missing_pilot_panel+", primary_id = "+primary_id+"]";
    }
}
