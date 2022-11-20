package tchat.microervices.ms_content_management.dtos.post;

import lombok.*;
import tchat.microervices.ms_content_management.beans.ImageVideo;
import tchat.microervices.ms_content_management.beans.Post;
import tchat.microervices.ms_content_management.vos.User;

import java.util.Date;

@NoArgsConstructor
@Data
public class PostResponseDTO {

    private String id;
    private String content;
    private ImageVideo imageVideo;
    private Long nbrShares;
    private Long nbrLikes;
    private Long nbrComments;
    private Date createdAt;
    private Date updatedAt;
    private User userCreatedPost;
    private User userSharedPost;
    private Post post;
    private boolean isTypeShare;
}
