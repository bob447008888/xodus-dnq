package com.jetbrains.teamsys.dnq.database;

/*Generated by MPS  */

import com.jetbrains.mps.dnq.common.tests.TestOnlyServiceLocator;
import com.jetbrains.teamsys.database.Entity;
import com.jetbrains.teamsys.dnq.association.PrimitiveAssociationSemantics;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import jetbrains.mps.internal.collections.runtime.Sequence;
import jetbrains.mps.internal.collections.runtime.IWhereFilter;

public class TestUserService {

  public static Entity findUser(final String username, final String password) {
    Iterable<Entity> users = Sequence.fromIterable(TestOnlyServiceLocator.getTransientEntityStore().getThreadSession().getAll("User")).where(new IWhereFilter<Entity>() {
      public boolean accept(Entity entity) {
        return ((String) PrimitiveAssociationSemantics.get(entity, "username", null)).equals(username) && ((String)PrimitiveAssociationSemantics.get(entity, "password", null)).equals(password);
      }
    });
    if(!(ListSequence.fromIterable(users).isEmpty())) {
      return Sequence.fromIterable(users).first();
    }
    return null;
  }
}
