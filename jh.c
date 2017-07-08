#include <stdio.h>
#include <stdlib.h>
#include <math.h>

int main() {

	int a;
	int b;
	int c;

	a=0;
	b=0;
	c=0;

	if( a==0 ) {
		a = a+1;
		if(b==0) {
			b=b+1;
			c=b+1;
		}

		if(b==0) {
			printf("dontknot\n");
		}
		else if(b==1) {
			printf("you are a good boy\n");
			c=c+5;
		}

		for(a=0;a<c;a=a+1) {
			printf("Fuck you\n");
		}

	}

	return 0;
}
