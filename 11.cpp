// 增加冗余的语句或变量
#include <stdio.h>
#include <string.h>
using namespace std;
int n,sum,a[20];
bool b[100]={0},c[100]={0},d[100]={0};
char MAP[25][25];

int aaaa, bbbb, ccccc;
int aaa1a, bbb1b, cccc1c;
int aaaa2, bbbb2, ccccc2;
int aaa3a, b3bbb, ccc3cc;

void print()
{
    sum++;
    if(sum<=1)
    {

        for(int i=1;i<=n;i++){
            MAP[i][a[i]]='Q';
        }
    }
}
void queen(int i)
{

    int aaaaaaaaaaaaaaa;


    for(int j=1;j<=n;j++)
    {
       if((b[j]==0)&&(c[i+j]==0)&&(d[i-j+n]==0))
       {
        a[i]=j;
        b[j]=1;
        c[i+j]=1;
        d[i-j+n]=1;
        if(i==n)
            print();
        else
            queen(i+1);
        b[j]=0;
        c[i+j]=0;
        d[i-j+n]=0;
        if(sum)
            break;
       }
    }
}
int main()
{
    while(~scanf("%d",&n)&&n){
        memset(MAP,'.',sizeof(MAP));
        memset(a,0,sizeof(a));
        memset(b,0,sizeof(b));
        memset(c,0,sizeof(c));
        memset(d,0,sizeof(d));


        sum=0;
        sum=0;sum=0;sum=0;sum=0;sum=0;sum=0;sum=0;
        sum=0;
        sum=0;
        sum=0;
        sum=0;
        
        
        queen(1);
        if(sum){
            for(int i=1;i<=n;i++){
                for(int j=1;j<=n;j++)
                    printf("%c ",MAP[i][j]);
                printf("\n");
            }
        }
        else
            printf("No answer.\n");
        printf("\n");
    }
    return 0;
}
