package anticope.rejects.commands;

import anticope.rejects.arguments.EnumStringArgumentType;
import anticope.rejects.utils.GiveUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Optional;

import static anticope.rejects.utils.accounts.PlayerSkinUtils.getHeadTexture;
import static anticope.rejects.utils.accounts.PlayerSkinUtils.getUUID;

public class GiveCommand extends Command {

    private final Collection<String> PRESETS = GiveUtils.PRESETS.keySet();

    public GiveCommand() {
        super("give", "Gives items in creative", "item", "kit");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // not transferring custom nbt from source item... yet
        builder.then(literal("egg").executes(ctx -> {
            ItemStack inHand = mc.player.getMainHandStack();
            ItemStack item = new ItemStack(Items.STRIDER_SPAWN_EGG);
            NbtCompound data = new NbtCompound();

            if (inHand.getItem() instanceof BlockItem) {
                NbtCompound blockState = new NbtCompound();
                blockState.putString("Name", Registries.ITEM.getId(inHand.getItem()).toString());

                data.putString("id", "minecraft:falling_block");
                data.putInt("Time", 1);
                data.put("BlockState", blockState);

            } else {
                NbtCompound itemData = new NbtCompound();
                itemData.putString("id", Registries.ITEM.getId(inHand.getItem()).toString());
                itemData.putInt("Count", inHand.getCount());

                data.putString("id", "minecraft:item");
                data.put("Item", itemData);
            }
            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, inHand.getName())
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(data))
                    .build();

            item.applyChanges(changes);
            GiveUtils.giveItem(item);
            return SINGLE_SUCCESS;
        }));

        // there is a lot of repeat here, gotta be a better way
        builder.then(literal("holo").then(argument("message", StringArgumentType.string()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.STRIDER_SPAWN_EGG);
            NbtCompound tag = new NbtCompound();
            NbtList pos = new NbtList();

            pos.add(NbtDouble.of(mc.player.getX()));
            pos.add(NbtDouble.of(mc.player.getY()));
            pos.add(NbtDouble.of(mc.player.getZ()));

            tag.putString("id", "minecraft:armor_stand");
            tag.put("Pos", pos);
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag))
                    .build();

            stack.applyChanges(changes);
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        }).then(argument("x", IntegerArgumentType.integer()).then(argument("y", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.STRIDER_SPAWN_EGG);
            NbtCompound tag = new NbtCompound();
            NbtList pos = new NbtList();

            pos.add(NbtDouble.of(Double.parseDouble(ctx.getArgument("x", String.class))));
            pos.add(NbtDouble.of(Double.parseDouble(ctx.getArgument("y", String.class))));
            pos.add(NbtDouble.of(Double.parseDouble(ctx.getArgument("z", String.class))));

            tag.putString("id", "minecraft:armor_stand");
            tag.put("Pos", pos);
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag))
                    .build();

            stack.applyChanges(changes);
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        }))))));

        builder.then(literal("bossbar").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            String message = ctx.getArgument("message", String.class).replace("&", "\247");
            ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
            NbtList activeEffects = new NbtList();
            NbtCompound tag = new NbtCompound();
            NbtCompound effect = new NbtCompound();

            effect.putInt("duration", 2147483647);
            effect.putInt("amplifier", 1);
            effect.putString("id", "minecraft:invisibility");
            activeEffects.add(effect);

            tag.put("active_effects", activeEffects);
            tag.putBoolean("NoAI", true);
            tag.putBoolean("Silent", true);
            tag.putBoolean("PersistenceRequired", true);
            tag.put("id", NbtString.of("minecraft:wither"));

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.CUSTOM_NAME, Text.literal(message))
                    .add(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag))
                    .build();

            stack.applyChanges(changes);
            GiveUtils.giveItem(stack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("head").then(argument("owner", StringArgumentType.greedyString()).executes(ctx -> {
            String playerName = ctx.getArgument("owner", String.class);
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);

            PropertyMap properties = new PropertyMap();
            properties.put("texture", new Property("textures", getHeadTexture(getUUID(playerName))));

            var changes = ComponentChanges.builder()
                    .add(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of(playerName), Optional.of(getUUID(playerName)), properties, new GameProfile(getUUID(playerName), playerName)))
                    .build();

            itemStack.applyChanges(changes);
            GiveUtils.giveItem(itemStack);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("preset").then(argument("name", new EnumStringArgumentType(PRESETS)).executes(context -> {
            String name = context.getArgument("name", String.class);
            GiveUtils.giveItem(GiveUtils.getPreset(name));
            return SINGLE_SUCCESS;
        })));
    }
}
