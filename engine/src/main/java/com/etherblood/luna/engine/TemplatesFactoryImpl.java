package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.data.ActionAnimation;
import com.etherblood.luna.engine.actions.data.ActionDuration;
import com.etherblood.luna.engine.actions.data.ActionEvent;
import com.etherblood.luna.engine.actions.data.ActionInterruptResist;
import com.etherblood.luna.engine.actions.data.ActionInterruptStrength;
import com.etherblood.luna.engine.actions.data.ActionKey;
import com.etherblood.luna.engine.actions.data.ActionOf;
import com.etherblood.luna.engine.actions.data.ActionRange;
import com.etherblood.luna.engine.actions.data.ActionSpeed;
import com.etherblood.luna.engine.actions.data.ActionTurnable;
import com.etherblood.luna.engine.actions.data.BaseCooldown;
import com.etherblood.luna.engine.actions.data.DeleteAfterActorAction;
import com.etherblood.luna.engine.behaviors.GhostBehavior;
import com.etherblood.luna.engine.controlflow.Spawner;
import com.etherblood.luna.engine.controlflow.Triggerbox;
import com.etherblood.luna.engine.controlflow.WaitForTrigger;
import com.etherblood.luna.engine.damage.DamageTrigger;
import com.etherblood.luna.engine.damage.Damagebox;
import com.etherblood.luna.engine.damage.DeleteSelfAfterDamageTrigger;
import com.etherblood.luna.engine.damage.Hitbox;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.movement.Movebox;
import com.etherblood.luna.engine.movement.Obstaclebox;
import com.etherblood.luna.engine.movement.Speed;

public class TemplatesFactoryImpl implements TemplatesFactory {

    @Override
    public void apply(GameEngine game, int entity, String templateKey) {
        EntityData data = game.getData();
        int fps = game.getRules().getFramesPerSecond();
        switch (templateKey) {
            case "challenge_room": {
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(5_000, -5_000, 10_000, 10_000)));
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(-15_000, -5_000, 10_000, 10_000)));
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(-5_000, 5_000, 10_000, 10_000)));
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(-5_000, -15_000, 10_000, 10_000)));

                int spawner1 = data.createEntity();
                data.set(spawner1, new Spawner("ghost"));
                data.set(spawner1, new Position(0, 15_000));
                data.set(spawner1, Team.OPPONENTS);
                data.set(spawner1, new BaseCooldown(569));
                break;
            }
            case "test_room": {
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(5_000, -5_000, 10_000, 30_000)));
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(-15_000, -5_000, 10_000, 30_000)));
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(-5_000, 25_000, 10_000, 10_000)));
                data.set(data.createEntity(), new Obstaclebox(new Rectangle(-5_000, -15_000, 10_000, 10_000)));

                int trigger1 = 1;
                data.set(data.createEntity(), new Triggerbox(trigger1, new Rectangle(-5_000, 5_000, 10_000, 10_000)));

                int spawner1 = data.createEntity();
                data.set(spawner1, new Spawner("ghost"));
                data.set(spawner1, new Position(-3_000, 14_000));
                data.set(spawner1, new WaitForTrigger(trigger1));
                data.set(spawner1, Team.OPPONENTS);

                int spawner2 = data.createEntity();
                data.set(spawner2, new Spawner("ghost"));
                data.set(spawner2, new Position(0, 15_000));
                data.set(spawner2, new WaitForTrigger(trigger1));
                data.set(spawner2, Team.OPPONENTS);

                int spawner3 = data.createEntity();
                data.set(spawner3, new Spawner("ghost"));
                data.set(spawner3, new Position(3_000, 14_000));
                data.set(spawner3, new WaitForTrigger(trigger1));
                data.set(spawner3, Team.OPPONENTS);


                int trigger2 = 2;
                data.set(data.createEntity(), new Triggerbox(trigger2, new Rectangle(-5_000, 15_000, 10_000, 10_000)));

                int spawner4 = data.createEntity();
                data.set(spawner4, new Spawner("ghost"));
                data.set(spawner4, new Position(-3_000, 24_000));
                data.set(spawner4, new WaitForTrigger(trigger2));
                data.set(spawner4, Team.OPPONENTS);

                int spawner5 = data.createEntity();
                data.set(spawner5, new Spawner("ghost"));
                data.set(spawner5, new Position(0, 25_000));
                data.set(spawner5, new WaitForTrigger(trigger2));
                data.set(spawner5, Team.OPPONENTS);

                int spawner6 = data.createEntity();
                data.set(spawner6, new Spawner("ghost"));
                data.set(spawner6, new Position(3_000, 24_000));
                data.set(spawner6, new WaitForTrigger(trigger2));
                data.set(spawner6, Team.OPPONENTS);

                int spawner7 = data.createEntity();
                data.set(spawner7, new Spawner("ghost"));
                data.set(spawner7, new Position(-3_000, 11_000));
                data.set(spawner7, new WaitForTrigger(trigger2));
                data.set(spawner7, Team.OPPONENTS);

                int spawner8 = data.createEntity();
                data.set(spawner8, new Spawner("ghost"));
                data.set(spawner8, new Position(0, 10_000));
                data.set(spawner8, new WaitForTrigger(trigger2));
                data.set(spawner8, Team.OPPONENTS);

                int spawner9 = data.createEntity();
                data.set(spawner9, new Spawner("ghost"));
                data.set(spawner9, new Position(3_000, 11_000));
                data.set(spawner9, new WaitForTrigger(trigger2));
                data.set(spawner9, Team.OPPONENTS);

                break;
            }
            case "amara": {
                int idleAction = data.createEntity();
                {
                    apply(game, idleAction, "idle_action");
                    data.set(idleAction, new ActionOf(entity));
                    data.set(idleAction, ActionKey.IDLE);
                    data.set(idleAction, new ActionAnimation("idle"));
                }

                {
                    int walkAction = data.createEntity();
                    apply(game, walkAction, "walk_action");
                    data.set(walkAction, new ActionOf(entity));
                    data.set(walkAction, ActionKey.WALK);
                    data.set(walkAction, new ActionSpeed(1100 / fps));
                    data.set(walkAction, new ActionAnimation("walk"));
                }

                {
                    int dashAction = data.createEntity();
                    apply(game, dashAction, "dash_action");
                    data.set(dashAction, new ActionOf(entity));
                    data.set(dashAction, ActionKey.DASH);
                    data.set(dashAction, new ActionSpeed(5000 / fps));
                    data.set(dashAction, new ActionDuration(48));
                    data.set(dashAction, new ActionAnimation("dash"));
                }

                {
                    int gazeOfDarknessAction = data.createEntity();
                    apply(game, gazeOfDarknessAction, "gaze_of_darkness_action");
                    data.set(gazeOfDarknessAction, new ActionOf(entity));
                    data.set(gazeOfDarknessAction, ActionKey.ATTACK1);
                    data.set(gazeOfDarknessAction, new ActionAnimation("attack1"));
                }

                {
                    int bladeOfChaosAction = data.createEntity();
                    apply(game, bladeOfChaosAction, "blade_of_chaos_action");
                    data.set(bladeOfChaosAction, new ActionOf(entity));
                    data.set(bladeOfChaosAction, ActionKey.ATTACK2);
                    data.set(bladeOfChaosAction, new ActionAnimation("attack2"));
                }

                {
                    int waveOfQuickHealingAction = data.createEntity();
                    apply(game, waveOfQuickHealingAction, "wave_of_quick_healing_action");
                    data.set(waveOfQuickHealingAction, new ActionOf(entity));
                    data.set(waveOfQuickHealingAction, ActionKey.ATTACK3);
                    data.set(waveOfQuickHealingAction, new ActionAnimation("agonizing"));
                }

                {
                    int fallenAction = data.createEntity();
                    apply(game, fallenAction, "fallen_action");
                    data.set(fallenAction, new ActionOf(entity));
                    data.set(fallenAction, ActionKey.FALLEN);
                    data.set(fallenAction, new ActionAnimation("death"));
                    data.set(fallenAction, new ActionDuration(179));
                    data.set(fallenAction, new ActionEvent(179, "suicide"));
                }


                data.set(entity, new Movebox(new Rectangle(-250, -250, 500, 500)));
                data.set(entity, new Hitbox(new Circle(0, 0, 250)));
                data.set(entity, new ActiveAction(idleAction, game.getFrame()));
                data.set(entity, Direction.UP);
                data.set(entity, new MilliHealth(50_000));
                data.set(entity, new ModelKey("amara"));
                break;
            }
            case "ghost": {
                int idleAction = data.createEntity();
                {
                    apply(game, idleAction, "idle_action");
                    data.set(idleAction, new ActionOf(entity));
                    data.set(idleAction, ActionKey.IDLE);
                    data.set(idleAction, new ActionAnimation("idle"));
                }

                {
                    int walkAction = data.createEntity();
                    apply(game, walkAction, "walk_action");
                    data.set(walkAction, new ActionOf(entity));
                    data.set(walkAction, ActionKey.WALK);
                    data.set(walkAction, new ActionSpeed(50));
                    data.set(walkAction, new ActionAnimation("fly_forward"));
                }

                {
                    int ghostMeleeAction = data.createEntity();
                    apply(game, ghostMeleeAction, "ghost_melee_action");
                    data.set(ghostMeleeAction, new ActionOf(entity));
                    data.set(ghostMeleeAction, ActionKey.ATTACK1);
                    data.set(ghostMeleeAction, new ActionAnimation("melee_attack"));
                }

                {
                    int ghostSpellAction = data.createEntity();
                    apply(game, ghostSpellAction, "ghost_spell_action");
                    data.set(ghostSpellAction, new ActionOf(entity));
                    data.set(ghostSpellAction, ActionKey.ATTACK2);
                    data.set(ghostSpellAction, new ActionAnimation("cast_spell"));
                }

                {
                    int fallenAction = data.createEntity();
                    apply(game, fallenAction, "fallen_action");
                    data.set(fallenAction, new ActionOf(entity));
                    data.set(fallenAction, ActionKey.FALLEN);
                    data.set(fallenAction, new ActionAnimation("die"));
                    data.set(fallenAction, new ActionDuration(100));
                    data.set(fallenAction, new ActionEvent(100, "suicide"));
                }

                data.set(entity, new Movebox(new Rectangle(-250, -250, 500, 500)));
                data.set(entity, new Hitbox(new Circle(0, 0, 250)));
                data.set(entity, new ActiveAction(idleAction, game.getFrame()));
                data.set(entity, Direction.DOWN);
                data.set(entity, new MilliHealth(10_000));
                data.set(entity, new ModelKey("ghost"));
                data.set(entity, new GhostBehavior());
                break;
            }
            case "idle_action": {
                // not much to do here...
                data.set(entity, new ActionTurnable());
                break;
            }
            case "walk_action": {
                data.set(entity, new ActionTurnable());
                data.set(entity, new ActionSpeed(10));// default, should be overwritten by caller
                break;
            }
            case "dash_action": {
                data.set(entity, new ActionSpeed(50));// default, should be overwritten by caller
                data.set(entity, new ActionDuration(60));
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.MEDIUM));
                data.set(entity, new ActionInterruptResist(Long.MAX_VALUE, ActionInterruptResist.LOW));
                break;
            }
            case "fallen_action": {
                data.set(entity, new ActionDuration(0));// default, should be overwritten by caller
                data.set(entity, new ActionEvent(0, "suicide"));// default, should be overwritten by caller
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.MAX));
                data.set(entity, new ActionInterruptResist(Long.MAX_VALUE, ActionInterruptResist.NONE));
                break;
            }
            case "gaze_of_darkness_action": {
                data.set(entity, new ActionDuration(140));
                data.set(entity, new ActionEvent(50, "gaze_of_darkness"));
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.LOW));
                data.set(entity, new ActionInterruptResist(80, ActionInterruptResist.MEDIUM));

                data.set(entity, new ActionRange(1500));
                data.set(entity, new BaseCooldown(300));
                break;
            }
            case "blade_of_chaos_action": {
                data.set(entity, new ActionDuration(80));
                data.set(entity, new ActionEvent(32, "blade_of_chaos"));
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.LOW));
                data.set(entity, new ActionInterruptResist(50, ActionInterruptResist.MEDIUM));

                data.set(entity, new ActionRange(500));
                data.set(entity, new BaseCooldown(300));
                break;
            }
            case "wave_of_quick_healing_action": {
                data.set(entity, new ActionDuration(500));
                data.set(entity, new ActionEvent(30, "wave_of_quick_healing"));
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.MEDIUM));
                data.set(entity, new ActionInterruptResist(210, ActionInterruptResist.MEDIUM));

                data.set(entity, new BaseCooldown(1200));
                break;
            }
            case "ghost_melee_action": {
                data.set(entity, new ActionDuration(72));
                data.set(entity, new ActionEvent(48, "ghost_melee"));
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.LOW));
                data.set(entity, new ActionInterruptResist(Long.MAX_VALUE, ActionInterruptResist.MEDIUM));

                data.set(entity, new ActionRange(750));
                break;
            }
            case "ghost_spell_action": {
                data.set(entity, new ActionDuration(80));
                data.set(entity, new ActionEvent(32, "ghost_spell"));
                data.set(entity, new ActionInterruptStrength(ActionInterruptResist.LOW));
                data.set(entity, new ActionInterruptResist(50, ActionInterruptResist.MEDIUM));

                data.set(entity, new ActionRange(500));
                data.set(entity, new BaseCooldown(240));
                break;
            }

            case "gaze_of_darkness": {
                data.set(entity, new Damagebox(new Circle(0, 0, 1_000), DamageTrigger.PER_FRAME, MathUtil.ceilDiv(1_000, fps)));
                data.set(entity, new PendingDelete(game.getFrame() + 5 * fps));
                data.set(entity, new ModelKey("gaze_of_darkness"));
                break;
            }
            case "blade_of_chaos": {
                data.set(entity, new Damagebox(new Circle(0, 0, 500), DamageTrigger.ON_COLLISION, 2_000));

                data.set(entity, new PendingDelete(game.getFrame() + 1 * fps));
                long milliMetresPerFrame = 6_000L / game.getRules().getFramesPerSecond();
                data.set(entity, new Speed(milliMetresPerFrame));
                data.set(entity, new ModelKey("blade_of_chaos"));
                break;
            }
            case "wave_of_quick_healing": {
                long frames = 180;
                data.set(entity, new Damagebox(new Circle(0, 0, 1_000), DamageTrigger.PER_FRAME, Math.floorDiv(-5_000, frames), true, false));
                data.set(entity, new PendingDelete(game.getFrame() + frames));
                data.set(entity, new DeleteAfterActorAction());
                data.set(entity, new ModelKey("gaze_of_darkness"));
                break;
            }
            case "ghost_spell": {
                data.set(entity, new Damagebox(new Circle(0, 0, 200), DamageTrigger.ON_COLLISION, 2_000));

                data.set(entity, new PendingDelete(game.getFrame() + 3 * fps));
                long milliMetresPerFrame = 4_000L / game.getRules().getFramesPerSecond();
                data.set(entity, new Speed(milliMetresPerFrame));
                data.set(entity, new ModelKey("ghost_spell"));
                data.set(entity, new DeleteSelfAfterDamageTrigger());
                break;
            }
            case "ghost_melee": {
                data.set(entity, new Damagebox(new Circle(0, 0, 500), DamageTrigger.PER_FRAME, 3_000));
                data.set(entity, new PendingDelete(game.getFrame()));
                break;
            }
            case "suicide": {
                data.set(entity, new PendingDeleteOwner(game.getFrame()));
                break;
            }
            default:
                throw new AssertionError(templateKey);
        }
    }
}
