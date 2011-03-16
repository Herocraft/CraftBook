// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.gates.logic;

import java.util.Random;
import org.bukkit.Server;
import org.bukkit.block.Block;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import static com.sk89q.craftbook.ic.TripleInputChipState.*;

public class RisingRandomBit extends AbstractIC {
    
    protected Random random = new Random();

    public RisingRandomBit(Server server, Block block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Rising Random Bit";
    }

    @Override
    public void trigger(ChipState chip) {
        if (input(chip, 0)) {
            output(chip, 0, random.nextBoolean());
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Block block) {
            return new RisingRandomBit(getServer(), block);
        }
    }

}
