package tchat.microervices.ms_content_management.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageVideo {
    private String path;
    private String type;
}
