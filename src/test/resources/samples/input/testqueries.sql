


-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A;
-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A ORDER BY R.G;
-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A ORDER BY S.A;
-- SELECT R.G, S.A FROM Sailors S, Reserves R WHERE R.G > S.A ORDER BY R.G, S.A;
-- SELECT User.I, Order.L, Detail.N FROM User, Order, Detail WHERE User.I = Order.K AND User.I = Detail.M;
-- SELECT User.I, Order.L, Detail.N FROM User, Order, Detail WHERE User.I = Order.K AND User.I = Detail.M ORDER BY Detail.N DESC ;

-- P1 Test1
-- SELECT * FROM Sailors;
-- SELECT Sailors.A FROM Sailors;
-- SELECT S.A FROM Sailors S;
-- SELECT * FROM Sailors S WHERE S.A < 3;
-- SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
-- SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
-- SELECT DISTINCT R.G FROM Reserves R;
-- SELECT * FROM Sailors ORDER BY Sailors.B;

-- P1 Test2
-- 23 test:
-- SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A ;
-- SELECT * FROM Sailors S1, Reserves R WHERE S1.A = R.G;
-- 23:
SELECT * FROM Sailors S1, Sailors S2, Reserves R WHERE S1.A < S2.A AND S1.A = R.G;
-- -- 24:
SELECT S1.A, S2.A, S3.A FROM Sailors S1, Sailors S2, Sailors S3 WHERE S1.A < S2.A AND S2.A < S3.A AND S3.A < 5;
-- -- 36:
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C, Boats.F, Boats.E;
-- -- 38:
SELECT B.F, B.D FROM Boats B ORDER BY B.D;
-- -- 39:
SELECT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;



-- SELECT * FROM Sailors;
-- SELECT Sailors.A FROM Sailors;
-- SELECT Boats.F, Boats.D FROM Boats;
-- SELECT Reserves.G, Reserves.H FROM Reserves;
-- SELECT * FROM Sailors WHERE Sailors.B >= Sailors.C;
-- SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C;
-- SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C AND Sailors.B < Sailors.C;



-- SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
-- SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D;
-- SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D AND Sailors.B < 150;
-- SELECT DISTINCT * FROM Sailors;
-- SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
-- SELECT B.F, B.D FROM Boats B ORDER BY B.D;
-- SELECT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;
-- SELECT DISTINCT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;