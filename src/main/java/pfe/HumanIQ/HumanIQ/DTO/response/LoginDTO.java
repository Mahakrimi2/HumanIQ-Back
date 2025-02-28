package pfe.HumanIQ.HumanIQ.DTO.response;

import lombok.Data;


@Data
public class LoginDTO {
    private String token;

    public LoginDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
