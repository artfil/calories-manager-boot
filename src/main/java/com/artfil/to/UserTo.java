package com.artfil.to;

import com.artfil.HasIdAndEmail;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static org.hibernate.validator.constraints.SafeHtml.WhiteListType.NONE;

@Value
@EqualsAndHashCode(callSuper = true)
public class UserTo extends BaseTo implements HasIdAndEmail, Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(min = 2, max = 100)
    @SafeHtml(whitelistType = NONE)
    String name;

    @Email
    @NotBlank
    @Size(max = 100)
    @SafeHtml(whitelistType = NONE) // https://stackoverflow.com/questions/17480809
    String email;

    @NotBlank
    @Size(min = 5, max = 32)
    String password;

    @Range(min = 10, max = 10000)
    @NotNull
    Integer caloriesPerDay;

    public UserTo(Integer id, String name, String email, String password, int caloriesPerDay) {
        super(id);
        this.name = name;
        this.email = email;
        this.password = password;
        this.caloriesPerDay = caloriesPerDay;
    }
}
