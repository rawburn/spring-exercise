package net.rawburn.reactive.domain;

/**
 * @author rawburn·rc
 */
public class User {

    private String name;

    private String idcard;

    private String phone;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIdcard() {
        return this.idcard;
    }

    public void setIdcard(final String idcard) {
        this.idcard = idcard;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }
}
