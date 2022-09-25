package scheme;

import arc.util.Log;
import mindustry.game.Schematics;
import mindustry.mod.Mod;
import mindustry.mod.Scripts;
import scheme.moded.ModedBinding;
import scheme.moded.ModedSchematics;
import scheme.ui.MapResizeFix;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Main extends Mod {

    public Main() {
        // well, after the 136th build, it became much easier
        maxSchematicSize = 512;

        // mod reimported through mods dialog
        if (schematics.getClass().getSimpleName().startsWith("Moded")) return;

        assets.load(schematics = m_schematics = new ModedSchematics());
        assets.unload(Schematics.class.getSimpleName()); // prevent dual loading
    }

    @Override
    public void init() {
        Backdoor.load();
        ServerIntegration.load();
        ModedBinding.load();
        SchemeVars.load();
        SchemeUpdater.load();
        MapResizeFix.load();

        ui.traces = traces;
        ui.schematics = schemas;
        ui.listfrag = listfrag;

        units.load();
        builds.load();
        keycomb.load();

        m_settings.apply(); // sometimes settings are not self-applying

        hudfrag.build(ui.hudGroup);
        listfrag.build(ui.hudGroup);
        shortfrag.build(ui.hudGroup);

        control.setInput(m_input.asHandler());
        renderer.addEnvRenderer(0, render::draw);

        if (ModedSchematics.requiresDialog) ui.showOkText("@rename.name", "@rename.text", () -> {});
        if (settings.getBool("welcome")) ui.showOkText("@welcome.name", "@welcome.text", () -> {});
        if (settings.getBool("check4update")) SchemeUpdater.check();

        try { // run main.js without the wrapper to access the constant values in the game console
            Scripts scripts = mods.getScripts();
            scripts.context.evaluateReader(scripts.scope, SchemeUpdater.script().reader(), "main.js", 0);
        } catch (Throwable e) { error(e); }
    }

    public static void log(String info) {
        app.post(() -> Log.infoTag("Scheme", info));
    }

    public static void error(Throwable info) {
        app.post(() -> Log.err("Scheme", info));
    }
}
