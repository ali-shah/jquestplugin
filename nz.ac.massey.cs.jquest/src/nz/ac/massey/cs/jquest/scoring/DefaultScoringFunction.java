/*
 * Copyright 2014 Ali Shah Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.gnu.org/licenses/agpl.html Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package nz.ac.massey.cs.jquest.scoring;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.Path;
import nz.ac.massey.cs.jdg.Dependency;

public class DefaultScoringFunction implements ScoringFunction {

	@Override
	public int getEdgeScore(Motif<TypeNode,Dependency> motif, String pathRole,Path<TypeNode,Dependency> path,Dependency edge) {
		return 1;
	}
	
	@Override
	public String toString() {
		return "simple scoring function (score is always 1)";
	}

}
