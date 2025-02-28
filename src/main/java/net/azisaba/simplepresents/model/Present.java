package net.azisaba.simplepresents.model;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Present {

    private final List<PresentItem> items;

    public Present(List<PresentItem> items) {
        this.items = items;
    }

    public void giveTo(Player player) {
        for (PresentItem item : items) {
            item.giveTo(player);
        }
    }

    public List<Map<String, Object>> serialize() {
        return items.stream()
                .map(PresentItem::serialize)
                .collect(Collectors.toList());
    }

    public static Present deserialize(List<Map<?, ?>> serializedItems) {
        List<PresentItem> items = new ArrayList<>();
        for (Map<?, ?> map : serializedItems) {
            items.add(PresentItem.deserialize((Map<String, Object>) map));
        }
        return new Present(items);
    }

    public List<PresentItem> getItems() {
        return items;
    }
}
