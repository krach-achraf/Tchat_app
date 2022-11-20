package tchat.microervices.ms_content_management.vos;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User{

    private Long id;

    private String fullname;

    private String username;

    private String photo;

    private String profession;

    private boolean isLocked;

    private boolean isExpired;

}
