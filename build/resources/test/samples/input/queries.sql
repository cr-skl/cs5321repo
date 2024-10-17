SELECT * FROM Sailors;
SELECT Sailors.A FROM Sailors;
SELECT S.A FROM Sailors S;
SELECT * FROM Sailors S WHERE S.A < 3;
SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
SELECT DISTINCT R.G FROM Reserves R;
SELECT * FROM Sailors ORDER BY Sailors.B;

SELECT User.I, Order.L, Detail.N FROM User, Order, Detail WHERE User.I = Order.K AND User.I = Detail.M;
SELECT User.I, Order.L, Detail.N FROM User, Order, Detail WHERE User.I = Order.K AND User.I = Detail.M ORDER BY Detail.N DESC;
