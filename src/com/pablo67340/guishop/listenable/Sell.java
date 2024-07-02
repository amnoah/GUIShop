package com.pablo67340.guishop.listenable;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.pablo67340.guishop.GUIShop;
import com.pablo67340.guishop.config.Config;
import com.pablo67340.guishop.definition.Item;
import com.pablo67340.guishop.definition.ItemSellReturn;
import com.pablo67340.guishop.definition.SellType;
import com.pablo67340.guishop.util.MathUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Sell {

    private ChestGui GUI;

    /**
     * Open the {@link Sell} GUI.
     *
     * @param player The player the GUI will display to
     */
    public void open(Player player) {
        GUI = new ChestGui(6, Config.getTitlesConfig().getSellTitle());
        GUI.setOnClose(this::onSellClose);
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        GUI.addPane(pane);
        GUI.show(player);
    }

    /**
     * Sell items inside the {@link Sell} GUI.
     *
     * @param player The player selling items
     */
    public void sell(Player player) {
        ItemSellReturn result = sellItems(player, GUI.getInventory().getContents(), SellType.INVENTORY);

        result.getNotSold().forEach(player.getInventory()::addItem);

        if (result.getCouldntSell()) {
            GUIShop.getINSTANCE().getMiscUtils().sendPrefix(player, "cant-sell", result.getColdntSellCount());
        }

        GUI.getInventory().clear();
    }

    /**
     * Sells the specified items on the behalf of a player
     *
     * @param player the player
     * @param items the items
     */
    public static ItemSellReturn sellItems(Player player, ItemStack[] items, SellType type) {
        List<ItemStack> checkedItems = Arrays.stream(items).filter(Objects::nonNull).collect(Collectors.toList());
        ArrayList<ItemStack> soldItems = new ArrayList<>();
        ArrayList<ItemStack> unsellableItems = new ArrayList<>();

        BigDecimal moneyToGive = BigDecimal.valueOf(0);
        boolean couldntSell = false;
        int countSell = 0;

        ConcurrentHashMap<Material, Integer> itemMap = new ConcurrentHashMap<>();

        for (ItemStack item : checkedItems) {
            Item shopItem = null;

            GUIShop.getINSTANCE().getLogUtil().debugLog("Checking if " + item.getType() + " is sellable");

            List<Item> itemList = GUIShop.getINSTANCE().getITEMTABLE().get(item.getType().toString());

            if (itemList != null) {
                for (Item itm : itemList) {
                    if (itm.isItemFromItemStack(item)) {
                        shopItem = itm;
                    }
                }
            }

            if (shopItem == null || !shopItem.hasSellPrice() || (!GUIShop.getINSTANCE().getMiscUtils().getPerms().playerHas(player, "guishop.shop." + shopItem.getShop()) && !GUIShop.getINSTANCE().getMiscUtils().getPerms().playerHas(player, "guishop.shop.*")) || (shopItem.hasPermission() && shopItem.getPermission().doesntHavePermission(player))) {
                countSell++;
                couldntSell = true;
                unsellableItems.add(item);
                continue;
            }

            soldItems.add(item);

            if (itemMap.containsKey(item.getType())) {
                int oldAmount = itemMap.get(item.getType());
                itemMap.put(item.getType(), oldAmount + item.getAmount());
            } else {
                itemMap.put(item.getType(), item.getAmount());
            }

            int quantity = item.getAmount();

            // buy price must be defined for dynamic pricing to work
            if (Config.isDynamicPricing() && shopItem.isUseDynamicPricing() && shopItem.hasBuyPrice()) {
                moneyToGive = moneyToGive.add(GUIShop.getINSTANCE().getMiscUtils().getDYNAMICPRICING().calculateSellPrice(item.getType().toString(), quantity,
                        shopItem.getBuyPriceAsDecimal(), shopItem.getSellPriceAsDecimal()));
                GUIShop.getINSTANCE().getMiscUtils().getDYNAMICPRICING().sellItem(item.getType().toString(), quantity);
            } else {
                moneyToGive = moneyToGive.add(shopItem.getSellPriceAsDecimal().multiply(BigDecimal.valueOf(quantity)));
            }
        }

        roundAndGiveMoney(player, moneyToGive);

        String materialsString = checkedItems.stream().map(item -> item.getType().toString()).collect(Collectors.joining(", "));

        int itemAmount = 0;

        for (Map.Entry<Material, Integer> entry : itemMap.entrySet()) {
            itemAmount += entry.getValue();
        }

        GUIShop.getINSTANCE().getLogUtil().transactionLog(
                "Player " + player.getName() + " sold " + itemAmount + " items (" + itemMap.size() + " different) for " + moneyToGive.toPlainString() + ". Inventory type:" + type.name() + " Items: \n" + materialsString);

        return new ItemSellReturn(unsellableItems, soldItems, couldntSell, countSell, moneyToGive);
    }

    /**
     * Rounds the amount and deposits it on behalf of the player.
     *
     * @param player the player
     * @param moneyToGive the amount to give
     */
    public static void roundAndGiveMoney(Player player, BigDecimal moneyToGive) {
        if (moneyToGive.compareTo(BigDecimal.ZERO) > 0) {
            GUIShop.getINSTANCE().getMiscUtils().getECONOMY().depositPlayer(player, moneyToGive.doubleValue());

            String amount = GUIShop.getINSTANCE().getConfigManager().getMessageSystem().translate("messages.currency-prefix")
                    + MathUtil.round(moneyToGive.doubleValue(), 2) + GUIShop.getINSTANCE().getConfigManager().getMessageSystem().translate("messages.currency-suffix");

            GUIShop.getINSTANCE().getMiscUtils().sendPrefix(player, "sell", amount);
        }
    }

    private void onSellClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        sell(player);
    }
}
