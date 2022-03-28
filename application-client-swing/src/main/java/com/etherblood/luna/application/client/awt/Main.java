package com.etherblood.luna.application.client.awt;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameLoop;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Speed;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String... args) {
        GameEngine engine = GameRules.getDefault().createGame();

        Gui gui = new Gui();

        EntityData data = engine.getData();
        int player = data.createEntity();
        int character = data.createEntity();
        data.set(character, new OwnedBy(player));
        data.set(character, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character, new Position(0, 0));
        data.set(character, new Speed(0, 0));
        data.set(character, new ActorState(ActorAction.IDLE, Direction.RIGHT, 0));

        GameLoop loop = new GameLoop(60, () -> {
            Set<Integer> keyCodes = gui.getPressedKeys();
            HashMap<Integer, PlayerInput> playerInputs = new HashMap<>();
            int x = 0;
            int y = 0;
            if (keyCodes.contains(KeyEvent.VK_UP)) {
                y++;
            }
            if (keyCodes.contains(KeyEvent.VK_DOWN)) {
                y--;
            }
            if (keyCodes.contains(KeyEvent.VK_RIGHT)) {
                x++;
            }
            if (keyCodes.contains(KeyEvent.VK_LEFT)) {
                x--;
            }
            PlayerInput input = new PlayerInput(Direction.of(x, y), ActorAction.IDLE);
            playerInputs.put(player, input);
            engine.tick(playerInputs);
            gui.render(createRenderTask(engine));
        });
        gui.start(keyEvent -> {
        }, null);
        loop.run();
    }

    private static RenderTask createRenderTask(GameEngine engine) {
        EntityData data = engine.getData();
        List<Rectangle> moveboxes = data.list(Movebox.class).stream()
                .map(entity -> {
                    Rectangle rectangle = data.get(entity, Movebox.class).rectangle();
                    return rectangle.translate(data.get(entity, Position.class).vector());
                })
                .toList();
        return new RenderTask(
                new Rectangle(0, 0, 6000, 4000),
                moveboxes,
                Color.BLACK
        );
    }
}
