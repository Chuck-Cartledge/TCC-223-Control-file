package Scripts;

import java.io.Serializable;

public class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    public String crmID;
    public String surname;
    public String givenName;
    public String telephone;
    public String postCode;

    public Client(String id, String sn, String gn, String tel, String pc) {
        this.crmID = id;
        this.surname = sn;
        this.givenName = gn;
        this.telephone = tel;
        this.postCode = pc;
    }
}