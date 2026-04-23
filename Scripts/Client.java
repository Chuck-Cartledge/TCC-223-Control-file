package Scripts;

import java.io.Serializable;

// holds one client record, no logic here
public class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    private String crmID;
    private String surname;
    private String givenName;
    private String telephone;
    private String postCode;

    public Client(String id, String sn, String gn, String tel, String pc) {
        this.crmID     = id;
        this.surname   = sn;
        this.givenName = gn;
        this.telephone = tel;
        this.postCode  = pc;
    }

    public String getCrmID()     { return crmID; }
    public String getSurname()   { return surname; }
    public String getGivenName() { return givenName; }
    public String getTelephone() { return telephone; }
    public String getPostCode()  { return postCode; }

    public void setCrmID(String crmID)         { this.crmID     = crmID; }
    public void setSurname(String surname)     { this.surname   = surname; }
    public void setGivenName(String givenName) { this.givenName = givenName; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setPostCode(String postCode)   { this.postCode  = postCode; }
}
