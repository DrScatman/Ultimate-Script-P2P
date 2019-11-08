package script;

import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import script.paint.ScriptPaint;
import script.quests.nature_spirit.NatureSpirit;
import script.quests.priest_in_peril.PriestInPeril;
import script.quests.the_restless_ghost.TheRestlessGhost;
import script.quests.waterfall_quest.WaterfallQuest;
import script.quests.witches_house.WitchesHouse;
import script.tasks.BuyItemsNeeded;
import script.tasks.BuySupplies;
import script.tasks.GetStartersGold;
import script.tasks.fungus.Fungus;
import script.tasks.training.magic.TrainTo13;
import script.tasks.training.prayer.TrainTo50;

import java.util.HashMap;

@ScriptMeta(developer = "Streagrem", name = "LOL", desc = "LOL")
public class Main extends TaskScript implements RenderListener {

    private ScriptPaint paint;
    private StopWatch runtime;
    private HashMap<String, Integer> ALL_ITEMS_NEEDED_FOR_ACCOUNT_PREPERATION;

    public StopWatch getRuntime() {
        return runtime;
    }

    @Override
    public void onStart() {
        runtime = StopWatch.start();
        paint = new ScriptPaint(this);

        setStartingItemsMap();
        setRestlessGhostItemsMap();
        setWitchesHouseItemsMap();
        setWaterfallItemsMap();
        setPriestInPerilItemsMap();
        setNatureSpiritItemsMap();

        submit(new BuySupplies(ALL_ITEMS_NEEDED_FOR_ACCOUNT_PREPERATION),
                new GetStartersGold(),
                new BuyItemsNeeded(),
                new TrainTo13()
        );

        submit(TheRestlessGhost.TASKS);
        submit(WitchesHouse.TASKS);
        submit(WaterfallQuest.TASKS);
        submit(PriestInPeril.TASKS);
        submit(NatureSpirit.TASKS);

        submit(new TrainTo50(),
                new Fungus()
        );
    }

    private void setRestlessGhostItemsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        TheRestlessGhost.setSupplyMap(map);
    }

    private void setWitchesHouseItemsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("Amulet of glory(6)", 1);
        map.put("Staff of air", 1);
        map.put("Ring of wealth (5)", 1);
        map.put("Cheese", 2);
        map.put("Mind rune", 100);
        map.put("Fire rune", 300);
        map.put("Falador teleport", 5);
        map.put("Leather gloves", 1);
        map.put("Tuna", 10);
        WitchesHouse.setSupplyMap(map);
    }

    private void setWaterfallItemsMap() {
        //TODO: Add items
        HashMap<String, Integer> map = new HashMap<>();
        WaterfallQuest.setSupplyMap(map);
    }

    private void setPriestInPerilItemsMap() {
        //TODO: Add items
        HashMap<String, Integer> map = new HashMap<>();
        PriestInPeril.setSupplyMap(map);
    }

    private void setNatureSpiritItemsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("Silver sickle", 1);
        map.put("Ghostspeak amulet", 1);
        NatureSpirit.setSupplyMap(map);
    }

    private void setStartingItemsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("Lumbridge teleport", 10);
        map.put("Staff of air", 1);
        map.put("Staff of fire", 1);
        map.put("Amulet of glory(6)", 5);
        map.put("Ring of wealth (5)", 2);
        map.put("Air rune", 1000);
        map.put("Mind rune", 1000);
        map.put("Water rune", 200);
        map.put("Fire rune", 200);
        map.put("Earth rune", 300);
        map.put("Tuna", 100);
        map.put("Stamina potion(4)", 10);
        map.put("Cheese", 2);
        map.put("Leather gloves", 1);
        map.put("Falador teleport", 5);
        map.put("Games necklace(8)", 1);
        map.put("Rope", 2);
        map.put("Adamant scimitar", 1);
        map.put("Ring of recoil", 1);
        map.put("Bucket", 1);
        map.put("Rune essence", 50);
        map.put("Varrock teleport", 5);
        map.put("Silver sickle", 1);
        map.put("Dragon bones", 300);
        map.put("Burning amulet(5)", 5);
        ALL_ITEMS_NEEDED_FOR_ACCOUNT_PREPERATION = map;
    }

    @Override
    public void notify(RenderEvent e) {
        try {
            if (runtime != null) {
                paint.notify(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
