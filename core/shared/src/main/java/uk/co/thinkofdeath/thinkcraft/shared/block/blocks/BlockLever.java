/*
 * Copyright 2014 Matthew Collins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.thinkofdeath.thinkcraft.shared.block.blocks;

import uk.co.thinkofdeath.thinkcraft.shared.Face;
import uk.co.thinkofdeath.thinkcraft.shared.IMapViewer;
import uk.co.thinkofdeath.thinkcraft.shared.Texture;
import uk.co.thinkofdeath.thinkcraft.shared.block.Block;
import uk.co.thinkofdeath.thinkcraft.shared.block.BlockFactory;
import uk.co.thinkofdeath.thinkcraft.shared.block.enums.LeverDirection;
import uk.co.thinkofdeath.thinkcraft.shared.block.helpers.RedstoneConnectible;
import uk.co.thinkofdeath.thinkcraft.shared.block.states.BooleanState;
import uk.co.thinkofdeath.thinkcraft.shared.block.states.EnumState;
import uk.co.thinkofdeath.thinkcraft.shared.block.states.StateKey;
import uk.co.thinkofdeath.thinkcraft.shared.block.states.StateMap;
import uk.co.thinkofdeath.thinkcraft.shared.model.Model;
import uk.co.thinkofdeath.thinkcraft.shared.model.ModelFace;

public class BlockLever extends BlockFactory {

    public final StateKey<Boolean> POWERED = stateAllocator.alloc("powered", new BooleanState());
    public final StateKey<LeverDirection> DIRECTION = stateAllocator.alloc("direction", new EnumState<>(LeverDirection.class));

    private final Texture cobblestone;
    private final Texture lever;

    public BlockLever(IMapViewer iMapViewer) {
        super(iMapViewer);
        cobblestone = mapViewer.getTexture("cobblestone");
        lever = mapViewer.getTexture("lever");
    }

    @Override
    protected Block createBlock(StateMap states) {
        return new BlockImpl(states);
    }

    private class BlockImpl extends Block implements RedstoneConnectible {

        BlockImpl(StateMap state) {
            super(BlockLever.this, state);
        }

        @Override
        public int getLegacyData() {
            int val = getState(DIRECTION).ordinal();
            if (getState(POWERED)) {
                val |= 0x8;
            }
            return val;
        }

        @Override
        public Model getModel() {
            if (model == null) {
                model = new Model();

                // Lever
                model.addFace(new ModelFace(Face.LEFT, lever, 6, 3, 4, 10, 9)
                        .setTextureSize(6, 6, 4, 10));
                model.addFace(new ModelFace(Face.RIGHT, lever, 6, 3, 4, 10, 7)
                        .setTextureSize(6, 6, 4, 10));
                model.addFace(new ModelFace(Face.FRONT, lever, 6, 3, 4, 10, 9)
                        .setTextureSize(6, 6, 4, 10));
                model.addFace(new ModelFace(Face.BACK, lever, 6, 3, 4, 10, 7)
                        .setTextureSize(6, 6, 4, 10));
                model.addFace(new ModelFace(Face.TOP, lever, 7, 7, 2, 2, 13)
                        .setTextureSize(7, 6, 2, 2));

                float ang = 45f;
                float offset = 3;
                if (getState(POWERED)) {
                    ang = 180 - ang;
                    offset = -3;
                }

                model = new Model().join(model.rotateX(ang), 0, offset, 3);

                // Base

                model.addFace(new ModelFace(Face.BACK, cobblestone, 5, 4, 6, 8, 13));
                model.addFace(new ModelFace(Face.LEFT, cobblestone, 13, 4, 3, 8, 11));
                model.addFace(new ModelFace(Face.RIGHT, cobblestone, 13, 4, 3, 8, 5));
                model.addFace(new ModelFace(Face.TOP, cobblestone, 5, 13, 6, 3, 12));
                model.addFace(new ModelFace(Face.BOTTOM, cobblestone, 5, 13, 6, 3, 4));

                // Final rotation

                LeverDirection direction = getState(DIRECTION);

                model.rotateY(direction.getRotationY() * 90)
                        .rotateZ(direction.getRotationZ() * 90)
                        .rotateX(direction.getRotationX() * 90);
            }
            return model;
        }

        @Override
        public boolean isRedstoneConnectible() {
            return true;
        }
    }
}
