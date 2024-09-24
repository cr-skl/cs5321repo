-- SELECT * FROM Sailors;
-- SELECT * FROM Sailors ORDER BY  Sailors.B;
-- SELECT * FROM Sailors S WHERE Sailors.A < 3 AND Sailors.A > 1;
-- -- SELECT * FROM Sailors S WHERE Sailors.B = 100 ;
-- SELECT Sailors.A,Sailors.B FROM Sailors S WHERE Sailors.B = 100 ;
-- SELECT Sailors.B,Sailors.A FROM Sailors S WHERE Sailors.B = 100 ;
-- SELECT Sailors.C,Sailors.B FROM Sailors S WHERE Sailors.B = 100 ;
-- SELECT R.H, S.B FROM Sailors S, Reserves R WHERE R.H > S.B;
-- SELECT * FROM Sailors S1, Sailors S2 WHERE S1.B = S2.B;
-- SELECT R.H, S.B FROM Sailors S, Reserves R WHERE R.H > S.B;


-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A;
-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A ORDER BY R.G;
-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A ORDER BY S.A;
-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A ORDER BY R.G, S.A;

SELECT * FROM Sailors;
SELECT Sailors.A FROM Sailors;
SELECT S.A FROM Sailors S;
SELECT * FROM Sailors S WHERE S.A < 3;
SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
SELECT DISTINCT R.G FROM Reserves R;
SELECT * FROM Sailors ORDER BY Sailors.B;