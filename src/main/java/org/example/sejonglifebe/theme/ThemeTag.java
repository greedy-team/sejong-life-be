package org.example.sejonglifebe.theme;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.sejonglifebe.tag.Tag;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tag_id", "theme_id"}))
public class ThemeTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private ThemeTag(Theme theme, Tag tag) {
        this.theme = theme;
        this.tag = tag;
    }

    public static ThemeTag createThemeTag(Theme theme, Tag tag) {
        ThemeTag themeTag = new ThemeTag(theme, tag);
        theme.getThemeTags().add(themeTag);

        return themeTag;
    }
}
