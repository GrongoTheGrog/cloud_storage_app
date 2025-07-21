package com.grongo.cloud_storage_app.models.sharedItems;


import com.grongo.cloud_storage_app.models.TimeStamps;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@Data
@Table(name = "t_shared_items")
@AllArgsConstructor
@NoArgsConstructor
public class SharedItem extends TimeStamps {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "item_id")
    private Item item;

    private FileRole fileRole;
}
