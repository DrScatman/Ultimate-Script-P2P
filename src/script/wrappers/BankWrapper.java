package script.wrappers;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BankWrapper {

    private static int bankValue = -1;
    private static int inventoryValue = -1;
    private static int startingValue;
    private static boolean isMuleing;
    private static int amountMuled;

    public static int getTotalValue() {
        return getBankValue() + getInventoryValue();
    }

    public static int getBankValue() {
        return Math.max(bankValue, 0);
    }

    public static int getInventoryValue() {
        return Math.max(inventoryValue, 0);
    }

    public static int getTotalValueGained() {
        return (getTotalValue() - (startingValue > 0 ? startingValue : getTotalValue()));
    }

    public static void updateBankValue() {
        int newValue = PriceCheckService.getBankValue();

        if (bankValue == -1) {
            startingValue += newValue;
        }

        bankValue = newValue;
    }

    public static void updateInventoryValue() {
        int newValue = PriceCheckService.getInventoryValue();

        if (inventoryValue == -1) {
            startingValue += newValue;
            //startValueTimer = StopWatch.start();
        }
        /*if (startValueTimer != null && startValueTimer.exceeds(Duration.ofSeconds(10))) {
            startingValue += newValue;
            startValueTimer = null;
        }*/

        inventoryValue = newValue;
    }

    private static void openAndDepositAll(boolean keepAllCoins, int numCoinsToKeep, boolean withdrawNoted, Set<String> set,  String... itemsToKeep) {
        //Log.fine("Depositing Inventory");
        while (!openNearest() && Game.isLoggedIn()) {
            if (WalkingWrapper.shouldBreakOnRunenergy()) {
                Movement.toggleRun(true);
            }
            Time.sleep(600, 1600);
        }

        Bank.depositInventory();
        Time.sleepUntil(Inventory::isEmpty, 8000);
        Time.sleep(300, 600);
        inventoryValue = 0;
        updateBankValue();


        if (numCoinsToKeep > 0) {
            Bank.withdraw(995, numCoinsToKeep);
            Time.sleepUntil(()
                            -> Inventory.contains(995) && Inventory.getCount(true, 995) >= numCoinsToKeep, 5000);
        }
        if (keepAllCoins) {
            Bank.withdrawAll(995);
            Time.sleepUntil(() -> Inventory.contains(995), 5000);
        }

        if (withdrawNoted) {
            if (Bank.getWithdrawMode() != Bank.WithdrawMode.NOTE) {
                Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
                Time.sleepUntil(() -> Bank.getWithdrawMode() == Bank.WithdrawMode.NOTE, 5000);
            }
        }

        if (itemsToKeep != null && itemsToKeep.length > 0) {
            for (String i : itemsToKeep) {
                if (Bank.contains(x -> x.getName().equalsIgnoreCase(i))) {
                    Bank.withdrawAll(x -> x.getName().equalsIgnoreCase(i));
                    Time.sleepUntil(() -> Inventory.contains(x -> x.getName().equalsIgnoreCase(i)), 6000);
                }
            }
        }

        if (set != null && set.size() > 0) {
            for (String i : set) {
                if (Bank.contains(x -> x.getName().equalsIgnoreCase(i))) {
                    Bank.withdrawAll(x -> x.getName().equalsIgnoreCase(i));
                    Time.sleepUntil(() -> Inventory.contains(x -> x.getName().equalsIgnoreCase(i)), 6000);
                }
            }
        }

        if (Bank.getWithdrawMode() != Bank.WithdrawMode.ITEM) {
            Bank.setWithdrawMode(Bank.WithdrawMode.ITEM);
            Time.sleepUntil(() -> Bank.getWithdrawMode() == Bank.WithdrawMode.ITEM, 5000);
        }

        updateBankValue();
        updateInventoryValue();
    }

    public static void openAndDepositAll(boolean keepAllCoins, Set<String> itemsToKeepSet) {
        openAndDepositAll(keepAllCoins, 0, true, itemsToKeepSet);
    }

    public static void openAndDepositAll(boolean keepAllCoins, String... itemsToKeep) {
        openAndDepositAll(keepAllCoins, 0, true, null, itemsToKeep);
    }

    public static void openAndDepositAll(boolean keepAllCoins, boolean withdrawNoted, String... itemsToKeep) {
        openAndDepositAll(keepAllCoins, 0, withdrawNoted, null, itemsToKeep);
    }

    public static void openAndDepositAll(int numCoinsToKeep, String... itemsToKeep) {
        openAndDepositAll(false, numCoinsToKeep,true, null, itemsToKeep);
    }

    public static void openAndDepositAll(boolean keepAllCoins) {
        openAndDepositAll(keepAllCoins, 0, true, null);
    }

    public static void openAndDepositAll(int numCoinsToKeep) {
        openAndDepositAll(false, numCoinsToKeep, true, null);
    }

    public static void openAndDepositAll(String... itemsToKeep) {
        openAndDepositAll(false, 0, true, null, itemsToKeep);
    }

    public static boolean openNearest() {
        if (Bank.isOpen()) {
            return true;
        }
        return Bank.open();
    }

    public static void withdrawSellableItems(Set<String> itemsToKeep) {
        if (!Bank.getWithdrawMode().equals(Bank.WithdrawMode.NOTE)) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleep(800, 1250);
        }

        Item[] sellables = Bank.getItems(i -> i.isExchangeable()
                && !itemsToKeep.contains(i.getName())
               /* && PriceCheckService.getPrice(i.getId()) != null
                && (PriceCheckService.getPrice(i.getId()).getSellAverage() * i.getStackSize() > 5000)*/);

        for (Item s : sellables) {
            Bank.withdrawAll(s.getName());
            Time.sleepUntil(() -> Inventory.contains(s.getName()), 1500, 8000);
        }

        updateBankValue();
        updateInventoryValue();
    }

    public static HashSet<String> getItemsNeeded(HashMap<String, Integer> itemsToBuy) {
        HashSet<String> set = new HashSet<>();
        if (itemsToBuy != null && itemsToBuy.size() > 0) {

            set = new HashSet<>(itemsToBuy.keySet());

            for (Map.Entry<String, Integer> i : itemsToBuy.entrySet()) {
                if (Bank.contains(x
                        -> x.getName().toLowerCase().equals(i.getKey().toLowerCase())
                            && x.getStackSize() >= i.getValue())) {

                    set.remove(i.getKey());
                }
            }
        }
        return set;
    }

    public static void setMuleing(boolean isMuleing) {
        BankWrapper.isMuleing = isMuleing;
    }

    public static boolean isMuleing() { return isMuleing; }

    public static int getAmountMuled() {
        return amountMuled;
    }

    public static void setAmountMuled(int amountMuled) {
        BankWrapper.amountMuled = amountMuled;
    }
}
