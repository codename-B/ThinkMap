package uk.co.thinkofdeath.mapviewer.shared.block.blocks;

import uk.co.thinkofdeath.mapviewer.shared.block.Block;
import uk.co.thinkofdeath.mapviewer.shared.block.BlockFactory;
import uk.co.thinkofdeath.mapviewer.shared.block.StateMap;
import uk.co.thinkofdeath.mapviewer.shared.block.states.BooleanState;
import uk.co.thinkofdeath.mapviewer.shared.block.states.IntegerState;

public class BlockLiquid extends BlockFactory {

    public final static String LEVEL = "level";
    public final static String FALLING = "falling";

    public BlockLiquid() {
        addState(LEVEL, new IntegerState(0, 7));
        addState(FALLING, new BooleanState());
    }

    @Override
    protected Block createBlock(StateMap states) {
        return new BlockImpl(this, states);
    }

    private class BlockImpl extends Block {
        public BlockImpl(BlockLiquid factory, StateMap states) {
            super(factory, states);
        }

        @Override
        public int getLegacyData() {
            return this.<Integer>getState(LEVEL)
                    | (this.<Boolean>getState(FALLING) ? 8 : 0);
        }
    }
}