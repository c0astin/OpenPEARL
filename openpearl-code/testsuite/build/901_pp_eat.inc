   eat: PROC(num FIXED);	  
!      REQUEST semaTermout;
!      OPEN termout;
      PUT 'Philosopher #', num, ': I am eating!' TO termout BY A, F(1), A, SKIP;
!      CLOSE termout;
!      RELEASE semaTermout;
      AFTER 0.5 SEC RESUME;
   END;
