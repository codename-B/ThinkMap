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

package uk.co.thinkofdeath.thinkcraft.html.worker.world;

import uk.co.thinkofdeath.thinkcraft.html.worker.Worker;
import uk.co.thinkofdeath.thinkcraft.shared.world.World;

public class WorkerWorld extends World {

    final Worker worker;

    /**
     * Creates a world designed to run on workers
     *
     * @param worker
     *         The worker which owns this world
     */
    public WorkerWorld(Worker worker) {
        super(worker);
        this.worker = worker;
    }
}