   think: PROC(num FIXED);
!      REQUEST semaTermout;
!      OPEN termout;
      PUT 'Philosopher #', num, ': I am thinking!' TO termout BY A, F(1), A, SKIP;
!      CLOSE termout;
!      RELEASE semaTermout;
      AFTER 1 SEC RESUME;
   END;
