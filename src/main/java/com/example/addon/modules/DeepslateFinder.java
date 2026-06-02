package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DeepslateFinder extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How many blocks to scan around you.")
        .defaultValue(32)
        .range(8, 256)
        .sliderRange(8, 256)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the highlight.")
        .defaultValue(Color.RED)
        .build()
    );

    public DeepslateFinder() {
        super(AddonTemplate.CATEGORY, "deepslate-finder", "Highlights deepslate blocks that are placed sideways (rotated).");
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        // Get the world and player
        if (mc.level == null || mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int range = this.range.get();

        // Only scan nearby blocks
        for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
            for (int y = playerPos.getY() - range; y <= playerPos.getY() + range; y++) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState blockState = mc.level.getBlockState(pos);

                    // Check if it's a deepslate block with rotated orientation
                    if (isRotatedDeepslate(blockState)) {
                        AABB box = new AABB(pos);
                        event.renderer.box(box, color.get(), color.get(), ShapeMode.Both, 0);
                    }
                }
            }
        }
    }

    private boolean isRotatedDeepslate(BlockState blockState) {
        // Check if block is a deepslate variant
        var block = blockState.getBlock();
        
        if (block == Blocks.DEEPSLATE || 
            block == Blocks.DEEPSLATE_BRICKS || 
            block == Blocks.DEEPSLATE_TILES) {
            
            // Check if it has a rotated property and is not facing up
            if (blockState.hasProperty(RotatedPillarBlock.AXIS)) {
                var axis = blockState.getValue(RotatedPillarBlock.AXIS);
                // Return true if not Y-axis (meaning it's rotated sideways)
                return axis != net.minecraft.core.Direction.Axis.Y;
            }
        }
        
        return false;
    }
}
