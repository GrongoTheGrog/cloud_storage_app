package com.grongo.cloud_storage_app.models.tag;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grongo.cloud_storage_app.models.items.Item;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "t_tag_item_join")
public class TagJoin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "item_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Item item;
    @ManyToOne
    @JoinColumn(name = "tag_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tag tag;
}
