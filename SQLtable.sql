create table rooms(
     roomid int primary key identity(1,1),
	 roomnumber int,
	 roombed int,
	 roomprice decimal(10,2),
	 roomstatus nvarchar(50),
);

create table guests(
    guestid int primary key identity(1,1),
	firstname nvarchar(50),
	lastname nvarchar(50),
	phone nvarchar(50),
);

create table bookings(
    bookingid int primary key identity(1,1),
	roomid int foreign key references rooms(roomid),
	guestid int foreign key references guests(guestid),
	checkindate date,
	checkoutdate date,
	bookstatus nvarchar(50),
);

create table payments(
    payid int primary key identity(1,1),
	guestid int foreign key references guests(guestid),
	amount decimal(10,2),
	paydate date,
);

create table users(
   userid int primary key identity(1,1),
   username nvarchar(100),
   password nvarchar(100),
   isadmin bit,
);

