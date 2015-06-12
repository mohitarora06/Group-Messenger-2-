# Group-Messenger-2-
Group messenger 2 is basically the extension of Group Messenger 1 with further improvements including:-

1) Ordering Constraint 2) Failure Handling


Ordering Constraint:- FIFO and TOTAL ordering constraint is applied on the messages recieved on different AVDs. ISIS
algorithm is used to implement this ordering constraint. B- Multicast technique is used to send messages to all the AVD instances.


Failure Handling:- Timer is used to handle failures in the system. This means that if the acknowledgment is not recieved in the given time frame then that node is cosidered to have failed and any state related to that is cleared.
