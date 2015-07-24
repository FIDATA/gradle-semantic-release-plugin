/*
 * Copyright 2015 the original author or authors.
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
package de.gliderpilot.gradle.semanticrelease

import com.github.zafarkhaja.semver.Version
import org.ajoberstar.gradle.git.release.semver.ChangeScope
import org.ajoberstar.gradle.git.release.semver.NearestVersion
import org.ajoberstar.gradle.git.release.semver.PartialSemVerStrategy
import org.ajoberstar.gradle.git.release.semver.SemVerStrategyState
import org.ajoberstar.gradle.git.release.semver.StrategyUtil
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException

import javax.script.*
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.TaskAction

class GradleSemanticReleaseStrategy implements PartialSemVerStrategy {

    Grgit grgit

    GradleSemanticReleaseStrategy(Grgit grgit) {
        this.grgit = grgit
    }

    @Override
    SemVerStrategyState infer(SemVerStrategyState state) {
        NearestVersion nearestVersion = state.nearestVersion
        Version previousVersion = nearestVersion?.normal
        if (!previousVersion || !previousVersion.majorVersion)
            return state.copyWith(inferredNormal: '1.0.0')

        if (!nearestVersion.distanceFromNormal) {
            // nothing has changed
            return state
        }


        List<Commit> log = grgit.log {
            range previousVersion.toString(), state.currentHead
        }

        if (log.any {})
            StrategyUtil.incrementNormalFromScope(state, ChangeScope.MAJOR)
        if (log.any {})
            StrategyUtil.incrementNormalFromScope(state, ChangeScope.MINOR)
        return StrategyUtil.incrementNormalFromScope(state, ChangeScope.PATCH)
    }
}