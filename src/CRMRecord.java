public class CRMRecord {

    private String givenName;
    private String surname;
    private String telephone;
    private String postcode;

    public CRMRecord(String givenName, String surname,
            String telephone, String postcode) {
        this.givenName = givenName;
        this.surname = surname;
        this.telephone = telephone;
        this.postcode = postcode;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Override
    public String toString() {
        return "Given Name: " + givenName + "\nSurname: " + surname + "\nTelephone: " + telephone + "\nPostcode: "
                + postcode + "\n";
    }
}