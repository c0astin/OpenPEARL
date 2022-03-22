taskPhilo1: TASK PRIO 5;
   TO 3 REPEAT
      REQUEST semaFork1, semaFork5;
      CALL eat(1);
      RELEASE semaFork1, semaFork5;
      CALL think(1);
   END;
END;

taskPhilo2: TASK PRIO 5;
   TO 3 REPEAT
      REQUEST semaFork2, semaFork1;
      CALL eat(2);
      RELEASE semaFork2, semaFork1;
      CALL think(2);
   END;
END;

taskPhilo3: TASK PRIO 5;
   TO 3 REPEAT
      REQUEST semaFork3, semaFork2;
      CALL eat(3);
      RELEASE semaFork3, semaFork2;
      CALL think(3);
   END;
END;

taskPhilo4: TASK PRIO 5;
   TO 3 REPEAT
      REQUEST semaFork4, semaFork3;
      CALL eat(4);
      RELEASE semaFork4, semaFork3;
      CALL think(4);
   END;
END;

taskPhilo5: TASK PRIO 5;
   TO 3 REPEAT
      REQUEST semaFork5, semaFork4;
      CALL eat(5);
      RELEASE semaFork5, semaFork4;
      CALL think(5);
   END;
END;
